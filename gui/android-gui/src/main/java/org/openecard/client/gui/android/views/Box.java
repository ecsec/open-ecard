/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.gui.android.views;

import android.content.Context;
import android.view.View;
import android.widget.GridView;
import org.openecard.client.gui.android.BoxItemAdapter;
import org.openecard.client.gui.definition.AbstractBox;
import org.openecard.client.gui.definition.OutputInfoUnit;

/**
 * Implementation of a radio- and checkbox group for use in a {@link StepActivity}.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Box implements StepView {

    private GridView gridview;
    private BoxItemAdapter boxItemAdapter;

    public Box(AbstractBox box, Context ctx, boolean useCheckBoxes) {
	gridview = new GridView(ctx){
	    //workaround to get a gridview displayed in a scrollview
	    @Override 
	    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	                 
	            // Calculate entire height by providing a very large height hint.
	            // But do not use the highest 2 bits of this integer; those are
	            // reserved for the MeasureSpec mode.
	            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
	                        MeasureSpec.AT_MOST);
	            super.onMeasure(widthMeasureSpec, expandSpec);

	            android.view.ViewGroup.LayoutParams params = getLayoutParams();
	            params.height = getMeasuredHeight();
	        
	    }
	};
	gridview.setColumnWidth(250);
	gridview.setNumColumns(-1); // auto fit
	gridview.setStretchMode(2); // stretch column
	boxItemAdapter = new BoxItemAdapter(ctx, box, useCheckBoxes);
	gridview.setAdapter(boxItemAdapter);
    }

    @Override
    public View getView() {
	return gridview;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public boolean isValueType() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	    return  boxItemAdapter.getValue();
    }
}
