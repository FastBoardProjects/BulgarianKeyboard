package com.maya.newbulgariankeyboard.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.adapters.LatestFavouriteLanguagesAdapter
import com.maya.newbulgariankeyboard.main_classes.LanguageModel
import com.maya.newbulgariankeyboard.main_classes.LatestLocaleHelper
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper
import com.maya.newbulgariankeyboard.main_utils.LatestFillerHelper
import com.maya.newbulgariankeyboard.media_inputs.LatestMediaHelper
import com.maya.newbulgariankeyboard.models.LatestFlagModel
import com.maya.newbulgariankeyboard.text_inputs.LatestInputHelper

class LatestFavouritesLanguagesFragment : Fragment(),
    TextWatcher {

    private val TAG = "LatestLangFrag:"
    private lateinit var mContext: Context
    private lateinit var languagesListView: RecyclerView
    private lateinit var statusLayout: ConstraintLayout
    private lateinit var myAnimation: LottieAnimationView
    var latestInputHelper: LatestInputHelper? = null
    var latestMediaHelper: LatestMediaHelper? = null
    private lateinit var editText: EditText
    private lateinit var delIcon: ImageView
    private var subtypes = ArrayList<LanguageModel>()
    private var listFlags = ArrayList<LatestFlagModel>()
    private var subtypesSearched = ArrayList<LanguageModel>()
    lateinit var keyboardLanguagesUtiler: LatestLocaleHelper
    private lateinit var prefs: LatestPreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mView = inflater.inflate(R.layout.fragment_languages, container, false)
        initViews(mView)
        return mView
    }

    @SuppressLint("LogNotTimber")
    private fun initViews(view: View) {

        prefs = LatestPreferencesHelper.getDefaultInstance(mContext)
        prefs.initAppPreferences()
        prefs.sync()
        keyboardLanguagesUtiler = LatestLocaleHelper(mContext, prefs)
        listFlags = LatestFillerHelper.fillFlagsModelList()
        try {
            latestInputHelper = LatestInputHelper.getInstance()
        } catch (e: Exception) {
        } /*crash*/
        latestMediaHelper = LatestMediaHelper.getInstance()
        editText = view.findViewById(R.id.editText)
        delIcon = view.findViewById(R.id.delIcon)
        statusLayout = view.findViewById(R.id.statusLayout)
        myAnimation = view.findViewById(R.id.myAnimation)
        myAnimation.enableMergePathsForKitKatAndAbove(true)
        languagesListView = view.findViewById(R.id.recyclerViewLanguages)
        languagesListView.addItemDecoration(
            DividerItemDecoration(
                mContext,
                DividerItemDecoration.VERTICAL
            )
        )
        editText.addTextChangedListener(this)
        delIcon.setOnClickListener {
            editText.setText("")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onResume() {
        super.onResume()
        subtypes = keyboardLanguagesUtiler.languageModels as ArrayList<LanguageModel>
        if (subtypes.isEmpty()) {
            statusLayout.visibility = View.VISIBLE
            languagesListView.visibility = View.GONE
            languagesListView.visibility = View.GONE
        } else {
            statusLayout.visibility = View.GONE
            languagesListView.visibility = View.VISIBLE
            languagesListView.layoutManager = GridLayoutManager(mContext, 1)
            val adapter =
                LatestFavouriteLanguagesAdapter(
                    mContext,
                    subtypes as ArrayList<LanguageModel>,
                    listFlags,
                    keyboardLanguagesUtiler
                )
            languagesListView.adapter = adapter
        }
        Log.d(TAG, "Current Lang: ${keyboardLanguagesUtiler!!.getActiveSubtype()!!.locale.toString()}")

    }

    override fun afterTextChanged(p0: Editable?) {

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        subtypesSearched.clear()
        val txt = p0?.toString()!!.trim().toLowerCase()
        if (txt.isNotEmpty()) {
            for (model in subtypes) {
                val displayName = model.locale.displayName
                if (displayName.toLowerCase().contains(txt)) {
                    subtypesSearched.add(model)
                }
            }
        } else {
            subtypesSearched.addAll(subtypes)
        }
        val adapter =
            LatestFavouriteLanguagesAdapter(
                mContext,
                subtypesSearched as ArrayList<LanguageModel>,
                listFlags,
                keyboardLanguagesUtiler
            )
        languagesListView.adapter = adapter
    }
}