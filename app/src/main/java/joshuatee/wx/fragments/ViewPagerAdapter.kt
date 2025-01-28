package joshuatee.wx.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import joshuatee.wx.settings.UIPreferences

class ViewPagerAdapter(activity: FragmentActivity?) : FragmentStateAdapter(activity!!) {

    val tabTitles = arrayOf("LOCAL", "SPC", "MISC")

    override fun getItemCount(): Int =
        if (UIPreferences.simpleMode || UIPreferences.navDrawerMainScreen) {
            1
        } else {
            tabTitles.size
        }

    override fun createFragment(position: Int): Fragment =
        if (UIPreferences.simpleMode || UIPreferences.navDrawerMainScreen) {
            LocationFragment()
        } else {
            when (position) {
                0 -> LocationFragment()
                1 -> SpcFragment()
                2 -> MiscFragment()
                else -> LocationFragment()
            }
        }

    fun setTabTitles(index: Int, title: String) {
        tabTitles[index] = title
    }
}
