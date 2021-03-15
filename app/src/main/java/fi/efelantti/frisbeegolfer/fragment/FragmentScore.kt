package fi.efelantti.frisbeegolfer.fragment

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.TypeConverter
import fi.efelantti.frisbeegolfer.Converters
import fi.efelantti.frisbeegolfer.R
import fi.efelantti.frisbeegolfer.model.CourseWithHoles
import fi.efelantti.frisbeegolfer.viewmodel.RoundViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModel
import fi.efelantti.frisbeegolfer.viewmodel.ScoreViewModelFactory
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class FragmentScore : Fragment() {

    companion object {
        private val converters = Converters()
        fun newInstance(roundId: OffsetDateTime): FragmentScore {
            val frag = FragmentScore()
            val args = Bundle()
            args.putString("roundId", converters.fromOffsetDateTime(roundId))
            frag.setArguments(args)
            return frag
        }
    }
    private lateinit var scoreViewModel: ScoreViewModel
    private val converters = Converters()
    private lateinit var testView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_score, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        var roundIdString = requireArguments().getString("roundId")
        var roundId = converters.toOffsetDateTime(roundIdString)
        if(roundId == null) throw IllegalArgumentException("Round id was null.")

        scoreViewModel = ViewModelProvider(this, ScoreViewModelFactory(this.requireActivity().application, roundId)).get(ScoreViewModel::class.java)

        testView = view.findViewById(R.id.fragment_score_test_textview)

        scoreViewModel.currentRound.observe(viewLifecycleOwner, Observer {
            if(it != null)
            {
                testView.text = it.round.dateStarted.toString()
            }
        })

        }
   }