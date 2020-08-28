package joshuatee.wx.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val tabTitles = arrayOf("LOCAL", "SPC", "MISC")

    override fun getCount() = if (MyApplication.simpleMode || UIPreferences.navDrawerMainScreen) 1 else tabTitles.size

    override fun getItem(position: Int): Fragment = if (MyApplication.simpleMode || UIPreferences.navDrawerMainScreen) {
            LocationFragment()
        } else {
            when (position) {
                0 -> LocationFragment()
                1 -> SpcFragment()
                2 -> MiscFragment()
                else -> LocationFragment()
            }
        }

    override fun getPageTitle(position: Int): CharSequence {
        tabTitles[0] = MyApplication.tabHeaders[0]
        return tabTitles[position]
    }

    fun setTabTitles(index: Int, title: String) {
        tabTitles[index] = title
    }
}
