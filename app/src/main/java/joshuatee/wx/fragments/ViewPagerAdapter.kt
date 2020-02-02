package joshuatee.wx.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import joshuatee.wx.MyApplication

//class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val tabTitles: Array<String> = arrayOf("LOCAL", "SPC", "MISC")

    override fun getCount(): Int {
        if (MyApplication.simpleMode) {
            return 1
        }
        return tabTitles.size
    }

    //override fun getItem(position: Int): Fragment? { before 'androidx.preference:preference:1.1.0' // was 1.0.0
    override fun getItem(position: Int): Fragment {
        if (MyApplication.simpleMode) {
            return LocationFragment()
        } else {
            when (position) {
                0 -> return LocationFragment()
                1 -> return SpcFragment()
                2 -> return MiscFragment()
            }
        }
        return LocationFragment()
        //return null
    }

    override fun getPageTitle(position: Int): CharSequence {
        tabTitles[0] = MyApplication.tabHeaders[0]
        return tabTitles[position]
    }

    fun setTabTitles(idx: Int, title: String) {
        tabTitles[idx] = title
    }
}
