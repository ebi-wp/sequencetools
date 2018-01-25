/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.RepositoryException;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.PeptideFeature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Segment;
import uk.ac.ebi.embl.api.entry.sequence.SegmentFactory;
import uk.ac.ebi.embl.api.translation.CdsTranslator;
import uk.ac.ebi.embl.api.translation.TranslationResult;
import uk.ac.ebi.embl.api.translation.Translator;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.CheckDataSet;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.RemoteExclude;

import java.util.*;

import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.GENE_QUALIFIER_NAME;
import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.PSEUDO_QUALIFIER_NAME;
import static uk.ac.ebi.embl.api.validation.SequenceEntryUtils.*;

@CheckDataSet(dataSetNames = { FileName.SINGLE_SOURCE_QUALIFIER })
@Description("Peptide feature sharing \\\"gene\\\" or \\\"locus_tag\\\" qualifier with CDS containing \\\"pseudo\\\" qualifier must also contain \\\"pseudo\\\" qualifier.\n" +
        "Translation of peptide feature must be a part of the translation of CDS feature unless \\\"exception\\\" or \\\"transl_except\\\" qualifiers are present\n" +
        "Translation of peptide feature must have a length equal to a multiple of 3\n" +
        "Translations are permitted to not be a multiple of 3 if the parent CDS feature is partial, as a start codon of 2 or 3 and the peptide feature has a start or end position equal to that of the parent CDS")
@RemoteExclude
public class PeptideFeatureCheck extends EntryValidationCheck {

    protected final static String NO_PSEUDO_MESSAGE = "PeptideFeatureCheck_1";
    protected static final String PEPTIDE_NOT_SUBSTRING_MESSAGE = "PeptideFeatureCheck_2";
    protected static final String NON_MOD_3_MESSAGE = "PeptideFeatureCheck_3";
    protected static final String NON_MOD_3_COMMENT = "PeptideFeatureCheck_4";

    private SegmentFactory segmentFactory;

    public ValidationResult check(Entry entry) throws ValidationEngineException {
    	try{
    	segmentFactory= new SegmentFactory(getEmblEntryValidationPlanProperty().enproConnection.get());
    	//System.out.println("peptide check begin");
        result = new ValidationResult();

        if (entry == null) {
            return result;
        }
        if(Entry.CON_DATACLASS.equals(entry.getDataClass())&&(entry.getSequence()==null||entry.getSequence().getSequenceByte()==null))
        {
        	 return result;
        }

        Collection<Feature> cdsFeatures = SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry);
        Collection<PeptideFeature> peptideFeatures = new ArrayList<PeptideFeature>();// a list of all peptide features
        List<Feature> features= entry.getFeatures();
        for (Feature feature : features) {

            if (feature instanceof PeptideFeature) {
                peptideFeatures.add((PeptideFeature) feature);
            }
        }

        if(peptideFeatures.isEmpty())
        {
        	return result;
        }
        /**
         * keep a hashmap of the cds features that are related to certain peptide features - will check the translations
         * against one another later on.
         */
        Map<Feature, List<PeptideFeature>> cdsToPeptideMap = new HashMap<Feature, List<PeptideFeature>>();

        for (Feature cdsFeature : cdsFeatures) {

            List<PeptideFeature> relevantPeptideFeatures =
                    new ArrayList<PeptideFeature>();// a list of peptide features relevant to the current CDS

            for (Feature peptideFeature : peptideFeatures) {

                /**
                 * look for peptide features sharing the same locus tag and on the same strand as the CDS...
                 */
                Qualifier cdsLocusQualifier =
                        SequenceEntryUtils.getQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, cdsFeature);
                Qualifier peptideLocusQualifier =
                        SequenceEntryUtils.getQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, peptideFeature);

                if (cdsLocusQualifier != null && peptideLocusQualifier != null &&
                        cdsLocusQualifier.getValue().equals(peptideLocusQualifier.getValue())) {

                    if (areLocationsOnSameStrand(
                            cdsFeature.getLocations(),
                            peptideFeature.getLocations())) {
                        relevantPeptideFeatures.add((PeptideFeature) peptideFeature);
                    }
                }

                /**
                 * ...then look for features sharing the same gene qualifier and have overlap in locations
                 */
                Qualifier cdsGeneQualifier =
                        SequenceEntryUtils.getQualifier(GENE_QUALIFIER_NAME, cdsFeature);
                Qualifier peptideGeneQualifier =
                        SequenceEntryUtils.getQualifier(GENE_QUALIFIER_NAME, peptideFeature);

                if (cdsGeneQualifier != null && peptideGeneQualifier != null &&
                        cdsGeneQualifier.getValue().equals(peptideGeneQualifier.getValue())) {

                    if (doLocationsOverlap(cdsFeature.getLocations(), peptideFeature.getLocations())) {
                        relevantPeptideFeatures.add((PeptideFeature) peptideFeature);
                    }
                }
            }
            cdsToPeptideMap.put(cdsFeature, relevantPeptideFeatures);
        }

        /**
         * now perform checks on the relevant peptides against each cds feature
         */
        if (!cdsToPeptideMap.isEmpty()) {
            for (Feature feature : cdsToPeptideMap.keySet()) {

                List<PeptideFeature> relevantPeptideFeatures = cdsToPeptideMap.get(feature);
                CdsFeature cdsFeature = (CdsFeature) feature;

                if (isQualifierAvailable(PSEUDO_QUALIFIER_NAME, cdsFeature)) {

                    /**
                     * if cds has pseudo, throw an error for every peptide feature that does not also have pseudo
                     */
                    for (PeptideFeature peptideFeature : relevantPeptideFeatures) {
                        if (!isQualifierAvailable(PSEUDO_QUALIFIER_NAME, peptideFeature)) {
                            ValidationMessage<Origin> message =
                                    reportError(peptideFeature.getOrigin(), NO_PSEUDO_MESSAGE,peptideFeature.getName());
                            message.getOrigins().add(cdsFeature.getOrigin());
                        }
                    }

                } else {//otherwise translate and check the peptides
                    CdsTranslator cdsTranslator = new CdsTranslator(getEmblEntryValidationPlanProperty());
                    try {

                        /**
                         * translate the CDS for comparisons later
                         */
                        ExtendedResult<TranslationResult> cdsValidationResult =
                                cdsTranslator.translate(cdsFeature, entry);

                        /**
                         * get the translator from the CDS translator for use with the peptides
                         */
                        Translator translator = new Translator();

                        for (PeptideFeature peptideFeature : relevantPeptideFeatures) {

                            Segment segment =
                                    segmentFactory.createSegment(entry.getSequence(), peptideFeature.getLocations());

                            translator.configureFromFeature(peptideFeature, getEmblEntryValidationPlanProperty().taxonHelper.get(), entry);

                            ExtendedResult<TranslationResult> peptideResult=null;
                            
							if (segment != null)
							{
								peptideResult = translator.translate(segment.getSequenceByte(), peptideFeature.getOrigin());
							}

                            /**
                             * append the validation results from translating the peptide, unlike the CDS results which we
                             * ignore (they will be picked up in the CDS checks.
                             */
                            result.append(peptideResult);

                            /**
                             * is the translation length a multiple of 3?...
                             */
                            if (peptideResult!=null&&peptideResult.getExtension().getTranslationLength() % 3 != 0) {
                                boolean peptideValid = false;

                                /**
                                 * ...if not, is the cds 5' or 3' partial?...
                                 */

                                /**
                                 * is 3' partial?
                                 */
                                if (cdsFeature.getLocations().isLeftPartial()) {

                                    Integer startCodon = cdsFeature.getStartCodon();
                                    /**
                                     * ...if partial, is the start codon 2 or 3?...
                                     */
                                    if (startCodon != null && (startCodon == 2 || startCodon == 3)) {
                                        /**
                                         * ...if start codon 2 or 3 do the start locations of the peptide and the cds match?
                                         */
                                        Location cdsStart = getStartFromLocations(cdsFeature.getLocations());
                                        Location peptideStart = getStartFromLocations(peptideFeature.getLocations());
                                        if (peptideStart.getBeginPosition().equals(cdsStart.getBeginPosition())) {
                                            //if they match then this peptide is valid
                                            peptideValid = true;
                                        }
                                    }
                                }

                                /**
                                 * is 5' partial?
                                 */
                                if (cdsFeature.getLocations().isRightPartial()) {
                                    /**
                                     * does the peptide end location match the end of a partial cds?
                                     */
                                    Location cdsEnd = getEndFromLocations(cdsFeature.getLocations());
                                    Location peptideEnd = getEndFromLocations(peptideFeature.getLocations());
                                    if (peptideEnd.getEndPosition().equals(cdsEnd.getEndPosition())) {
                                        //if they match then this peptide is valid
                                        peptideValid = true;
                                    }
                                }

                                if (!peptideValid) {
                                    ValidationMessage<Origin> message =
                                            reportError(peptideFeature.getOrigin(), NON_MOD_3_MESSAGE);
                                    message.setCuratorMessage(NON_MOD_3_COMMENT);
                                }
                            }

                            if (peptideResult!=null&&peptideResult.getExtension() != null && cdsValidationResult.getExtension() != null) {

                                String peptideTranslationSequence = peptideResult.getExtension().getSequence();
                                String cdsTranslationSequence = cdsValidationResult.getExtension().getSequence();

                                if (!cdsTranslationSequence.contains(peptideTranslationSequence)) {
                                    //is there an /exception or /transl_except qualifier?
                                    if (!isQualifierAvailable(Qualifier.EXCEPTION_QUALIFIER_NAME, cdsFeature) &&
                                            !isQualifierAvailable(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME, cdsFeature)) {
                                        ValidationMessage<Origin> message =
                                                reportError(peptideFeature.getOrigin(), PEPTIDE_NOT_SUBSTRING_MESSAGE);
                                        message.getOrigins().add(cdsFeature.getOrigin());
                                    }
                                }
                            }
                        }

                    } catch (RepositoryException e) {
                        //do nothing - other checks for translations
                    } catch (ValidationException e) {
                        //do nothing - other checks for translations
                    }
                }
            }
        }
    	}catch(Exception e)
    	{
    		throw new ValidationEngineException(e);
    	}
   //System.out.println("peptide check finish............");
        return result;
    }

    /**
     * a simplistic implementation to check for strandedness. This just looks for any case where one strand differs
     * from another and returns false. A more complex implementation would check for whether a fragment of the location
     * on one strand overlaps with a fragment from the other location on the same strand but this one just wants the
     * whole thing to be on the same strand for both locations.
     *
     * @param location1
     * @param location2
     * @return
     */
    private boolean areLocationsOnSameStrand(CompoundLocation<Location> location1,
                                             CompoundLocation<Location> location2) {

        boolean location1GlobalComplement = location1.isComplement();
        boolean location2GlobalComplement = location2.isComplement();

        if (location1GlobalComplement != location2GlobalComplement) {
            return false;
        }

        for (Location location : location1.getLocations()) {
            for (Location secondLocation : location2.getLocations()) {
                if (location.isComplement() != secondLocation.isComplement()) {
                    return false;//bail as soon as there is a mismatch
                }
            }
        }

        return true;
    }

}
