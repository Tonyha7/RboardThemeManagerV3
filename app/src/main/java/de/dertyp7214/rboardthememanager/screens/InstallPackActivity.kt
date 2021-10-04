package de.dertyp7214.rboardthememanager.screens

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.preferencesplus.core.dp
import com.topjohnwu.superuser.io.SuFile
import de.dertyp7214.rboardthememanager.R
import de.dertyp7214.rboardthememanager.adapter.ThemeAdapter
import de.dertyp7214.rboardthememanager.components.MarginItemDecoration
import de.dertyp7214.rboardthememanager.core.decodeBitmap
import de.dertyp7214.rboardthememanager.core.install
import de.dertyp7214.rboardthememanager.core.isInstalled
import de.dertyp7214.rboardthememanager.data.ThemeDataClass
import de.dertyp7214.rboardthememanager.databinding.ActivityInstallPackBinding
import de.dertyp7214.rboardthememanager.utils.doAsync
import java.io.File
import java.util.regex.Pattern

class InstallPackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstallPackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstallPackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fab = binding.fab
        val toolbar = binding.toolbar
        val recyclerview = binding.recyclerview

        toolbar.title = getString(R.string.install_themes, "0")

        val themes = arrayListOf<ThemeDataClass>()

        val adapter =
            ThemeAdapter(this, themes, ThemeAdapter.SelectionState.SELECTING, { _, adapter ->
                toolbar.title =
                    getString(R.string.install_themes, adapter.getSelected().size.toString())
            }) {}

        toolbar.navigationIcon = ContextCompat.getDrawable(
            this,
            R.drawable.ic_baseline_arrow_back_24
        )
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.select_all) {
                if (adapter.getSelected().size == themes.size) adapter.clearSelection()
                else adapter.selectAll()
            }
            true
        }

        fab.setOnClickListener {
            val success = adapter.getSelected().map { it.install() }
            if (success.contains(false)) Toast.makeText(this, R.string.error, Toast.LENGTH_LONG)
                .show()
            else Toast.makeText(this, R.string.themes_installed, Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        }

        recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) fab.hide() else if (dy < 0) fab.show()
            }
        })

        doAsync({
            intent.getStringArrayListExtra("themes")?.let { paths ->
                val list = arrayListOf<ThemeDataClass>()
                paths.forEach { themePath ->
                    val file = File(themePath)
                    val name = file.nameWithoutExtension
                    val path = file.absolutePath
                    val imageFile = SuFile(file.absolutePath.removeSuffix(".zip"))
                    val image = imageFile.exists().let {
                        Log.d("IMAGE", "$it $name")
                        if (it) {
                            imageFile.decodeBitmap()
                        } else null
                    }
                    list.add(ThemeDataClass(image, name, path))
                }
                list
            } ?: listOf()
        }) {
            val meta = File(File(it.firstOrNull()?.path ?: "").parent, "pack.meta")
            themes.clear()
            themes.addAll(it)
            adapter.notifyDataChanged()
            themes.forEachIndexed { index, themeDataClass ->
                adapter[index] = !themeDataClass.isInstalled()
            }
            adapter.notifyDataChanged()
            toolbar.title =
                getString(R.string.install_themes, adapter.getSelected().size.toString())
            if (meta.exists()) {
                var name: String? = null
                var author: String? = null
                meta.readText().apply {
                    val matcher = Pattern.compile("(name|author)=(.*)\\n").matcher(this)
                    while (matcher.find()) {
                        when (matcher.group(1)) {
                            "name" -> matcher.group(2)?.let { metaName ->
                                name = metaName
                            }
                            "author" -> matcher.group(2)?.let { metaAuthor ->
                                author = metaAuthor
                            }
                        }
                    }
                }
                if (name != null || author != null) {
                    toolbar.subtitle = "$name by $author"
                }
            }
        }

        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.setHasFixedSize(true)
        recyclerview.adapter = adapter
        recyclerview.addItemDecoration(MarginItemDecoration(2.dp(this), all = true))
    }
}