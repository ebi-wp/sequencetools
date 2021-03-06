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
package uk.ac.ebi.embl.api.validation.fixer.sourcefeature;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.helper.DataSetHelper;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.validation.FileName;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;
import uk.ac.ebi.ena.taxonomy.taxon.TaxonFactory;

import java.sql.SQLException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaxonomicDivisionNotQualifierFixTest
{

	private Entry entry;
	private SourceFeature sourceFeature;
	FeatureFactory featureFactory;
	private TaxonomicDivisionNotQualifierFix check;
	private EmblEntryValidationPlanProperty property;
	private TaxonHelper taxonHelper;

	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		EntryFactory entryFactory = new EntryFactory();
		featureFactory = new FeatureFactory();
		entry = entryFactory.createEntry();
		sourceFeature = featureFactory.createSourceFeature();
		property=new EmblEntryValidationPlanProperty();
		taxonHelper=createMock(TaxonHelper.class);
		property.taxonHelper.set(taxonHelper);

		DataRow dataRow1 = new DataRow(Qualifier.LAT_LON_QUALIFIER_NAME, "HUM");
		DataRow dataRow2 = new DataRow("dev_stage", "PRO");

		DataSetHelper.createAndAdd(FileName.TAXONOMIC_DIVISION_NO_QUALIFIER, dataRow1,dataRow2);
		check = new TaxonomicDivisionNotQualifierFix();
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_NoFeatures()
	{
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoPrimaryFeature()
	{
		entry.addFeature(featureFactory.createFeature(Feature.CDS_FEATURE_NAME));
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_NoDivision() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		assertTrue(check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_NoDivisionQualifier() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		sourceFeature.setSingleQualifierValue(Qualifier.LAT_LON_QUALIFIER_NAME, "Akio Tani");
		check.setEmblEntryValidationPlanProperty(property);
		ValidationResult result=check.check(entry);
		assertTrue(result.getMessages().size()==0);
	}

	@Test
	public void testCheck_DivisionNotQualifier() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		taxon.setDivision("HUM");
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("TaxonomicDivisionNotQualifierFix", Severity.ERROR));
	}

	@Test
	public void testCheck_DivisionInvalidQualifier() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		taxon.setDivision("HUM");
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		sourceFeature.setSingleQualifierValue(Qualifier.LAT_LON_QUALIFIER_NAME, "Akio Tani");
		ValidationResult result = check.check(entry);
		assertEquals(1, result.count("TaxonomicDivisionNotQualifierFix", Severity.FIX));
	}

	@Test
	public void testCheck_DivisionValidQualifier() throws SQLException
	{
		TaxonFactory taxonFactory=new TaxonFactory();
		Taxon taxon=taxonFactory.createTaxon();
		taxon.setDivision("HUM");
		sourceFeature.setScientificName("Homo sapiens");
		entry.addFeature(sourceFeature);
		expect(taxonHelper.getTaxonByScientificName("Homo sapiens")).andReturn(taxon);
		replay(taxonHelper);
		property.taxonHelper.set(taxonHelper);
		check.setEmblEntryValidationPlanProperty(property);
		Feature feature=featureFactory.createFeature(Feature.CDS_FEATURE_NAME);
		feature.setSingleQualifierValue(Qualifier.CITATION_QUALIFIER_NAME, "Akio Tani");
		ValidationResult result = check.check(entry);
		assertEquals(0, result.count("TaxonomicDivisionNotQualifierCheck_1", Severity.ERROR));
	}
}