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
package uk.ac.ebi.embl.api.entry.genomeassembly;


public class AssemblyInfoEntry extends GCSEntry
{
	private String submissionId;
	private String name;
	private String setId;
	private String projectId;
	private String wgsId;	
	private String masterId;
	private String sampleId;
	private String submissionAccountId;
	private Float coverage;
	private Integer minGapLength;
	private Integer contigCount;
	private Integer scaffoldCount;
	private Integer chromosomeCount;	
	private String contigAccRange;
	private String scaffoldAccRange;
	private String chromosomeAccRange;
	private String biosampleId;
	private Integer minContigCount;
	private Integer minScaffoldCount;
	private String organism;
	private String gcId;
	private String assemblyMethod;
	private String sequencingTechnology;
	private String program;
	private String platform;
	
	public String getAssemblyMethod()
	{
		return assemblyMethod;
	}

	public void setAssemblyMethod(String assemblyMethod)
	{
		this.assemblyMethod = assemblyMethod;
	}

	public String getSequencingTechnology()
	{
		return sequencingTechnology;
	}

	public void setSequencingTechnology(String sequencingTechnology)
	{
		this.sequencingTechnology = sequencingTechnology;
	}

	public String getProgram()
	{
		return program;
	}

	public void setProgram(String program)
	{
		this.program = program;
	}

	public String getPlatform()
	{
		return platform;
	}

	public void setPlatform(String platform)
	{
		this.platform = platform;
	}

		
	public String getGcId() {
		return gcId;
	}

	public void setGcId(String gcId) {
		this.gcId = gcId;
	}

	public Integer getMinContigCount() {
		return minContigCount;
	}

	public void setMinContigCount(Integer minContigCount) {
		this.minContigCount = minContigCount;
	}

	public Integer getMinScaffoldCount() {
		return minScaffoldCount;
	}

	public void setMinScaffoldCount(Integer minScaffoldCount) {
		this.minScaffoldCount = minScaffoldCount;
	}
	
	public String getSubmissionId() 
	{
		return submissionId;
	}
	
	public void setSubmissionId(String submissionId) 
	{
		this.submissionId = submissionId;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	public String getSetId() 
	{
		return setId;
	}
	
	public void setSetId(String setId) 
	{
		this.setId = setId;
	}
	
	public String getProjectId() 
	{
		return projectId;
	}
	
	public void setProjectId(String projectId) 
	{
		this.projectId = projectId;
	}
		
	public String getWgsId() 
	{
		return wgsId;
	}

	public void setWgsId(String wgsId) 
	{
		this.wgsId = wgsId;
	}

	public String getMasterId() 
	{
		return masterId;
	}

	public void setMasterId(String masterId) 
	{
		this.masterId = masterId;
	}
	
	public String getSampleId() 
	{
		return sampleId;
	}

	public void setSampleId(String sampleId) 
	{
		this.sampleId = sampleId;
	}

	public String getSubmissionAccountId() 
	{
		return submissionAccountId;
	}

	public void setSubmissionAccountId(String submissionAccountId) 
	{
		this.submissionAccountId = submissionAccountId;
	}

	public Float getCoverage() 
	{
		return coverage;
	}
	
	public void setCoverage(Float coverage) 
	{
		this.coverage = coverage;
	}	

	public Integer getMinGapLength() 
	{
		return minGapLength;
	}

	public void setMinGapLength(Integer minGapLength) 
	{
		this.minGapLength = minGapLength;
	}
	
	public Integer getContigCount() 
	{
		return contigCount;
	}

	public void setContigCount(Integer contigCount) 
	{
		this.contigCount = contigCount;
	}

	public Integer getScaffoldCount() 
	{
		return scaffoldCount;
	}

	public void setScaffoldCount(Integer scaffoldCount) 
	{
		this.scaffoldCount = scaffoldCount;
	}
	
	
	public Integer getChromosomeCount() 
	{
		return chromosomeCount;
	}

	public void setChromosomeCount(Integer chromosomeCount) 
	{
		this.chromosomeCount = chromosomeCount;
	}

	public String getContigAccRange() 
	{
		return contigAccRange;
	}

	public void setContigAccRange(String contigAccRange) 
	{
		this.contigAccRange = contigAccRange;
	}

	public String getScaffoldAccRange() 
	{
		return scaffoldAccRange;
	}

	public void setScaffoldAccRange(String scaffoldAccRange) 
	{
		this.scaffoldAccRange = scaffoldAccRange;
	}

	public String getChromosomeAccRange() 
	{
		return chromosomeAccRange;
	}

	public void setChromosomeAccRange(String chromosomeAccRange) 
	{
		this.chromosomeAccRange = chromosomeAccRange;
	}

	public String getOrganism() 
	{
		return organism;
	}

	public void setOrganism(String organism) 
	{
		this.organism = organism;
	}
	public String getBiosampleId() {
		return biosampleId;
	}

	public void setBiosampleId(String biosampleId) {
		this.biosampleId = biosampleId;
	}
}