package joshuatee.wx.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import joshuatee.wx.MyApplication

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    val tabTitles = arrayOf("LOCAL", "SPC", "MISC", "IMAGE")

    override fun getCount(): Int {
        if (MyApplication.simpleMode) {
            return 1
        }
        return 4
    }

    override fun getItem(position: Int): Fragment? {
        if (MyApplication.simpleMode) {
            return LocationFragment()
        } else {
            when (position) {
                0 -> return LocationFragment()
                1 -> return SPCFragment()
                2 -> return MiscFragment()
                3 -> return ImagesFragmentGOES()
            }
        }
        return null
    }

    override fun getPageTitle(position: Int): CharSequence {
        tabTitles[0] = MyApplication.tabHeaders[0]
        tabTitles[3] = MyApplication.tabHeaders[3]
        return tabTitles[position]
    }

    fun setTabTitles(idx: Int, title: String) {
        tabTitles[idx] = title
    }
}
