package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import fi.efelantti.frisbeegolfer.databinding.FragmentCollectionObjectBinding
import fi.efelantti.frisbeegolfer.databinding.FragmentGameBinding

class FragmentGame : Fragment() {
    private lateinit var gameFragmentAdapter: GameFragmentAdapter
    private lateinit var viewPager: ViewPager2
    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        gameFragmentAdapter = GameFragmentAdapter(this)
        viewPager = binding.pager
        gameFragmentAdapter.addFragment(FragmentScore())
        gameFragmentAdapter.addFragment(FragmentScoreTable())

        viewPager.adapter = gameFragmentAdapter
        val tabLayout: TabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "OBJECT ${(position + 1)}"
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class GameFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragmentList: ArrayList<Fragment> = ArrayList()

    fun addFragment(fragment: Fragment) {
        fragmentList.add(fragment)
    }

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}

private const val ARG_OBJECT = "object"

// Instances of this class are fragments representing a single
// object in our collection.
class DemoObjectFragment : Fragment() {

    private var _binding: FragmentCollectionObjectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionObjectBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            val textView = binding.text1
            textView.text = getInt(ARG_OBJECT).toString()
        }
    }
}
