[H[2Jdiff --git a/app/src/main/java/joshuatee/wx/WX.kt b/app/src/main/java/joshuatee/wx/WX.kt

// [FIX] ViewPagerAdapter: Switch to ViewPager2 and use FragmentStateAdapter instead. https://stackoverflow.com/questions/56778106/fragmentpageradapter-deprecated
// https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2
// https://developer.android.com/reference/androidx/viewpager2/adapter/FragmentStateAdapter
// https://developer.android.com/training/animation/vp2-migration#kotlin


index 01603bfa..06dfad89 100644
--- a/app/src/main/java/joshuatee/wx/WX.kt
+++ b/app/src/main/java/joshuatee/wx/WX.kt
@@ -40,8 +40,10 @@ import androidx.core.view.GravityCompat
 import androidx.drawerlayout.widget.DrawerLayout
 import androidx.localbroadcastmanager.content.LocalBroadcastManager
 import androidx.viewpager.widget.ViewPager
+import androidx.viewpager2.widget.ViewPager2
 import com.google.android.material.navigation.NavigationView
 import com.google.android.material.tabs.TabLayout
+import com.google.android.material.tabs.TabLayoutMediator
 import joshuatee.wx.activitiesmisc.*
 import joshuatee.wx.canada.CanadaAlertsActivity
 import joshuatee.wx.common.GlobalVariables
@@ -71,7 +73,7 @@ class WX : CommonActionBarFragment() {
     private lateinit var navigationView: NavigationView
     private lateinit var drawerLayout: DrawerLayout
     private lateinit var slidingTabLayout: TabLayout
-    private lateinit var viewPager: ViewPager
+    private lateinit var viewPager: ViewPager2
 
     override fun onCreate(savedInstanceState: Bundle?) {
         setTheme(UIPreferences.themeInt)
@@ -112,10 +114,14 @@ class WX : CommonActionBarFragment() {
             fab.visibility = View.GONE
         }
         viewPager.offscreenPageLimit = 4
-        vpa = ViewPagerAdapter(supportFragmentManager)
+        vpa = ViewPagerAdapter(this)
         viewPager.adapter = vpa
         slidingTabLayout.tabGravity = TabLayout.GRAVITY_FILL
-        slidingTabLayout.setupWithViewPager(viewPager)
+//        slidingTabLayout.setupWithViewPager(viewPager)
+        TabLayoutMediator(slidingTabLayout, viewPager) { tab, position ->
+            tab.text = "OBJECT ${(position + 1)}"
+        }.attach()
+
         slidingTabLayout.elevation = MyApplication.elevationPref
         if (MyApplication.simpleMode || UIPreferences.hideTopToolbar || UIPreferences.navDrawerMainScreen) {
             slidingTabLayout.visibility = View.GONE
diff --git a/app/src/main/java/joshuatee/wx/fragments/ViewPagerAdapter.kt b/app/src/main/java/joshuatee/wx/fragments/ViewPagerAdapter.kt
index 082a5123..1eafc863 100644
--- a/app/src/main/java/joshuatee/wx/fragments/ViewPagerAdapter.kt
+++ b/app/src/main/java/joshuatee/wx/fragments/ViewPagerAdapter.kt
@@ -1,22 +1,24 @@
 package joshuatee.wx.fragments
 
 import androidx.fragment.app.Fragment
+import androidx.fragment.app.FragmentActivity
 import androidx.fragment.app.FragmentManager
 import androidx.fragment.app.FragmentPagerAdapter
+import androidx.viewpager2.adapter.FragmentStateAdapter
 import joshuatee.wx.MyApplication
 import joshuatee.wx.UIPreferences
 
-class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
+class ViewPagerAdapter(activity: FragmentActivity?) : FragmentStateAdapter(activity!!) {
 
     val tabTitles = arrayOf("LOCAL", "SPC", "MISC")
 
-    override fun getCount() = if (MyApplication.simpleMode || UIPreferences.navDrawerMainScreen) {
+    override fun getItemCount() = if (MyApplication.simpleMode || UIPreferences.navDrawerMainScreen) {
         1
     } else {
         tabTitles.size
     }
 
-    override fun getItem(position: Int): Fragment = if (MyApplication.simpleMode || UIPreferences.navDrawerMainScreen) {
+    override fun createFragment(position: Int): Fragment = if (MyApplication.simpleMode || UIPreferences.navDrawerMainScreen) {
             LocationFragment()
         } else {
             when (position) {
@@ -27,10 +29,10 @@ class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_
             }
         }
 
-    override fun getPageTitle(position: Int): CharSequence {
-        tabTitles[0] = UIPreferences.tabHeaders[0]
-        return tabTitles[position]
-    }
+//    override fun getPageTitle(position: Int): CharSequence {
+//        tabTitles[0] = UIPreferences.tabHeaders[0]
+//        return tabTitles[position]
+//    }
 
     fun setTabTitles(index: Int, title: String) {
         tabTitles[index] = title
diff --git a/app/src/main/res/layout/activity_main.xml b/app/src/main/res/layout/activity_main.xml
index 77168561..f9905c70 100644
--- a/app/src/main/res/layout/activity_main.xml
+++ b/app/src/main/res/layout/activity_main.xml
@@ -47,7 +47,7 @@
                 android:height="?attr/actionBarSize"
                 android:background="?attr/colorPrimary" />
 
-            <androidx.viewpager.widget.ViewPager
+            <androidx.viewpager2.widget.ViewPager2
                 android:id="@+id/viewPager"
                 android:layout_width="match_parent"
                 android:layout_height="match_parent" />
diff --git a/app/src/main/res/layout/activity_main_drawer.xml b/app/src/main/res/layout/activity_main_drawer.xml
index 228f5c7e..608db347 100644
--- a/app/src/main/res/layout/activity_main_drawer.xml
+++ b/app/src/main/res/layout/activity_main_drawer.xml
@@ -56,7 +56,7 @@
                     android:height="?attr/actionBarSize"
                     android:background="?attr/colorPrimary" />
 
-                <androidx.viewpager.widget.ViewPager
+                <androidx.viewpager2.widget.ViewPager2
                     android:id="@+id/viewPager"
                     android:layout_width="match_parent"
                     android:layout_height="match_parent" />
diff --git a/app/src/main/res/layout/activity_main_drawer_right.xml b/app/src/main/res/layout/activity_main_drawer_right.xml
index a56b8634..0dcd0639 100644
--- a/app/src/main/res/layout/activity_main_drawer_right.xml
+++ b/app/src/main/res/layout/activity_main_drawer_right.xml
@@ -56,7 +56,7 @@
                     android:height="?attr/actionBarSize"
                     android:background="?attr/colorPrimary" />
 
-                <androidx.viewpager.widget.ViewPager
+                <androidx.viewpager2.widget.ViewPager2
                     android:id="@+id/viewPager"
                     android:layout_width="match_parent"
                     android:layout_height="match_parent" />
On branch master
Your branch is up to date with 'origin/master'.

Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git restore <file>..." to discard changes in working directory)
	modified:   ../app/src/main/java/joshuatee/wx/WX.kt
	modified:   ../app/src/main/java/joshuatee/wx/fragments/ViewPagerAdapter.kt
	modified:   ../app/src/main/res/layout/activity_main.xml
	modified:   ../app/src/main/res/layout/activity_main_drawer.xml
	modified:   ../app/src/main/res/layout/activity_main_drawer_right.xml

Untracked files:
  (use "git add <file>..." to include in what will be committed)
	viewPagerDiff.md

no changes added to commit (use "git add" and/or "git commit -a")
