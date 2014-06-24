/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.siri.android.util;

/**
 * Java imports
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

import edu.usf.cutr.siri.android.ui.MainActivity;
import edu.usf.cutr.siri.android.ui.R;

/**
 * This class holds utility methods for the application
 * 
 * @author Sean J. Barbeau
 * 
 */
public class SiriUtilsUI extends SiriUtils {

	/**
	 * Try to grab the developer key from an unversioned resource file, if it
	 * exists
	 * 
	 * @return the developer key from an unversioned resource file, or empty
	 *         string if it doesn't exist
	 */
	public static String getKeyFromResource(Context context) {
		String strKey = new String("");

		try {
			InputStream in = context.getResources().openRawResource(R.raw.devkey);
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringBuilder total = new StringBuilder();

			while ((strKey = r.readLine()) != null) {
				total.append(strKey);
			}

			strKey = total.toString();

			strKey.trim(); // Remove any whitespace

		} catch (NotFoundException e) {
			Log.w(MainActivity.TAG,
					"Warning - didn't find the developer key file:" + e);
		} catch (IOException e) {
			Log.w(MainActivity.TAG,
					"Error reading the developer key file:" + e);
		}

		return strKey;
	}
}
