package io.ak1.pixsample.samples.settings

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import io.ak1.pixsample.R
import io.ak1.pixsample.databinding.SettingsActivityBinding
import android.content.Intent
import android.view.MenuItem

import io.ak1.pixsample.MainActivity





class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this@SettingsActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val editTextPreference = EditTextPreference.OnBindEditTextListener { editText ->
            editText.inputType =
                InputType.TYPE_CLASS_NUMBER

        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val count: EditTextPreference? = findPreference("count")
            val videoDuration: EditTextPreference? = findPreference("videoDuration")
            count?.setOnBindEditTextListener(editTextPreference)
            videoDuration?.setOnBindEditTextListener(editTextPreference)
        }
    }
}