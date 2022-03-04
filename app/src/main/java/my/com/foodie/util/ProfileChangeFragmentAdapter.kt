package my.com.foodie.util


import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import my.com.foodie.ui.MyReviewFragment
import my.com.foodie.ui.ProfileDetailsFragment


class ProfileChangeFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                ProfileDetailsFragment()
            }
            1 -> {
                MyReviewFragment()
            }
            else -> ProfileDetailsFragment()
        }
    }
}

