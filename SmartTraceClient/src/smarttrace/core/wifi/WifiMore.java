/*
 * (C) Copyright University of Cyprus. 2010-2011.
 *
 * Android API
 *
 * @version         : 1.0
 * @author : Costantinos Costa(costa.costantinos@gmail.com)
 * Project Supervision : Demetris Zeinalipour (dzeina@cs.ucy.ac.cy)
 * Computer Science Department , University of Cyprus
 *
 *
 */
/*
 * This is a spatio-temporal similarity search framework, coined SmartTrace.
 *Our framework can be utilized to promptly answer queries
 *of the form: “Report the objects (i.e., trajectories) that follow
 *a similar spatio-temporal motion to Q, where Q is some query
 *trajectory.” SmartTrace, relies on an in-situ data storage model,
 *where spatio-temporal data remains on the smartphone that
 *generated the given data, as well a state-of-the-art top-K query
 *processing algorithm, which exploits distributed trajectory similarity
 *measures in order to identify the correct answer promptly.
 *
 *Copyright (C) 2010 - 2011 Costantinos Costa
 *This program is free software: you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation, either version 3 of the License, or
 *at your option) any later version.
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *GNU General Public License for more details.
 *Υou should have received a copy of the GNU General Public License
 *along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package smarttrace.core.wifi;

import smarttrace.core.R;
import smarttrace.core.R.drawable;
import smarttrace.core.R.id;
import smarttrace.core.R.layout;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WifiMore extends Activity {
	public static String terminal="";
	private TextView textView;
	private Button btnClear;
	private Button btnAbout;
	private Button btnBack;
	private Button btncon;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wmore);
		textView = (TextView) findViewById(R.id.textView);
		textView.setMovementMethod(new ScrollingMovementMethod()); 
		textView.setText("");
		btnClear = (Button) findViewById(R.id.clear);
		btnClear.setBackgroundResource(R.drawable.selector_clear);
		btnAbout = (Button) findViewById(R.id.about);
		btnAbout.setBackgroundResource(R.drawable.selector_about);
		btnBack = (Button) findViewById(R.id.back);
		btnBack.setBackgroundResource(R.drawable.selector_back);
		btncon = (Button) findViewById(R.id.btncon);
		btncon.setBackgroundResource(R.drawable.selector_terminal);
		initComp();
	}

	private void initComp() {
		textView.setScrollContainer(true);
		btnClear.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				textView.setTextColor(Color.WHITE);
				textView.setText("Clearing...\n\n");
			}
		});
		btnAbout.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				textView.setTextColor(0xFF00EE76);
				textView.setText("\n\nThe application created by...\n\n" );
				textView.append("\tCosta Costantinos\n");
				textView.append("\tZeinalipour Demetrios\n");
				textView.append("\tLaoudias Christos\n");
				textView.append("\n\t\tThank you\n");
				
				
			}
		});
		btnBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		btncon.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				textView.setTextColor(Color.WHITE);
				if(terminal.equals(""))
					textView.setText("None transaction yet..");
					else
				textView.setText(terminal);
			}
		});
		
	}

}