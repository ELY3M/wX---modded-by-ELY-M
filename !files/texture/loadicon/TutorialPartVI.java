/**
 * Copyright 2010 Per-Erik Bergman (per-erik.bergman@jayway.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package joshuatee.wx.radar.loadicon;

import joshuatee.wx.radar.loadicon.mesh.LoadIcon;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;


/**
 * This class is the setup for the Tutorial part VI located at:
 * http://blog.jayway.com/
 * 
 * @author Per-Erik Bergman (per-erik.bergman@jayway.com)
 * 
 */
public class TutorialPartVI extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		// Create a OpenGL view.
		GLSurfaceView view = new GLSurfaceView(this);

		OpenGLRenderer renderer = new OpenGLRenderer();
		view.setRenderer(renderer);

		setContentView(view);

		// LoadIcon.....
		LoadIcon plane = new LoadIcon(1, 1);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeFile(Constants.FilesPath+"location.png", options);
        // Load the texture.
        plane.loadBitmap(bitmap);
        renderer.x = 0;
        renderer.y = 0;
		renderer.addMesh(plane);

	}




}