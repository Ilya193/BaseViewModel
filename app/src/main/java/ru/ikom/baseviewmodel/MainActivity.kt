package ru.ikom.baseviewmodel

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ru.ikom.baseviewmodel.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), BaseView<MainViewModel.Model> {

    private val binding: ActivityMainBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModels()

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

        settingViewModel()
        setupViews()

        savedInstanceState?.let {
            viewModel.handleEvent(MainViewModel.Event.Recover())
        }
    }

    private fun settingViewModel() {
        lifecycleScope.launch {
            viewModel.action.collect {
                receiveAction(it)
            }
        }
    }

    private fun setupViews() {
        binding.items.adapter = adapter
        binding.items.itemAnimator = null
        binding.items.setHasFixedSize(true)
        binding.items.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
    }

    private fun receiveAction(action: MainViewModel.Action) {
        when (action) {
            is MainViewModel.Action.Render -> render(action)
        }
    }


    override val viewRenderer: ViewRenderer<MainViewModel.Model> by lazy(LazyThreadSafetyMode.NONE) {
        diff {
            diff(
                get = MainViewModel.Model::items,
                compare = { a, b -> a === b },
                set = adapter::submitList
            )

            diff(
                get = MainViewModel.Model::textTitle,
                set = binding.textview::setText
            )
        }
    }

    private fun render(action: MainViewModel.Action.Render) {
        viewRenderer.render(action.new)
    }
}