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
package uk.ac.ebi.embl.flatfile.reader.embl;

import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.FlatFileUtils;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.MultiLineBlockReader;

/** Reader for the flat file RG lines.
 */
public class RGReader extends MultiLineBlockReader {
	
	public RGReader(LineReader lineReader) {
		super(lineReader, ConcatenateType.CONCATENATE_SPACE);
	}
		
	@Override
	public String getTag() {
		return EmblTag.RG_TAG;
	}
	
	@Override
	protected void read(String block) {
		block = FlatFileUtils.remove(block, '"');
		block = FlatFileUtils.trimRight(block, ';');
		block = block.trim();
		if (!block.equals("")) {
			getCache().getPublication().setConsortium(block);
		}
	}
}