package ru.ikom.baseviewmodel

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.ikom.baseviewmodel.databinding.ActivityMainBinding

interface MainView : BaseView<MainView.Model> {

    data class Model(
        val items: List<ItemUi>,
        val textTitle: String
    )
}

val stateToModel: MainViewModel.State.() -> MainView.Model =
    {
        MainView.Model(
            items = items,
            textTitle = textTitle,
        )
    }

class MainActivity : AppCompatActivity(), MainView {

    private val binding: ActivityMainBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModels()

    override var viewRenderer: ViewRenderer<MainView.Model>? = null

    private val adapter = ItemsAdapter {
        viewModel.handleEvent(MainViewModel.Event.OnClick(it))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewRenderer = diff {
            diff(
                get = MainView.Model::items,
                compare = { a, b -> a === b },
                set = adapter::submitList
            )

            diff(
                get = MainView.Model::textTitle,
                set = binding.textview::setText
            )
        }

        settingViewModel()
        setupViews()

        viewModel.handleEvent(MainViewModel.Event.OnViewCreated())
    }

    private fun settingViewModel() {
        lifecycleScope.launch {
            launch {
                viewModel.states.map(stateToModel) bindTo ::render
            }

            launch {
                viewModel.labels.collect {
                    when (it) {
                        is MainViewModel.Label.Lock -> binding.lock.isVisible = true
                        is MainViewModel.Label.Unlock -> binding.lock.isVisible = false
                    }
                }
            }
        }
    }

    private fun setupViews() {
        binding.items.adapter = adapter
        binding.items.itemAnimator = null
        binding.items.setHasFixedSize(true)
        binding.items.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))

        binding.lock.setOnTouchListener { v, event -> true }
    }
}