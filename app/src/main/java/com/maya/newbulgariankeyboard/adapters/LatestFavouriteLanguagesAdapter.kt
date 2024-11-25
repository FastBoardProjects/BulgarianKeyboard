package com.maya.newbulgariankeyboard.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.maya.newbulgariankeyboard.R
import com.maya.newbulgariankeyboard.database.LatestRoomDatabase
import com.maya.newbulgariankeyboard.database.SubtypesDao
import com.maya.newbulgariankeyboard.main_classes.LanguageModel
import com.maya.newbulgariankeyboard.main_classes.LatestLanguageDbModel
import com.maya.newbulgariankeyboard.main_classes.LatestLocaleHelper
import com.maya.newbulgariankeyboard.models.LatestFlagModel
import de.hdodenhof.circleimageview.CircleImageView
import net.igenius.customcheckbox.CustomCheckBox

class LatestFavouriteLanguagesAdapter :
    RecyclerView.Adapter<LatestFavouriteLanguagesAdapter.FavLanguagesViewHolder> {


    private var mContext: Context
    private var list: ArrayList<LanguageModel>
    private var listFlags: ArrayList<LatestFlagModel>
    private var latestLocaleHelper: LatestLocaleHelper
    private lateinit var subtypesDao: SubtypesDao


    constructor(
        mContext: Context, list: ArrayList<LanguageModel>, listFlags: ArrayList<LatestFlagModel>,
        latestLocaleHelper: LatestLocaleHelper,
    ) : super() {
        this.mContext = mContext
        this.list = list
        this.listFlags = listFlags
        this.latestLocaleHelper = latestLocaleHelper
        subtypesDao = LatestRoomDatabase.getInstance(mContext).subtypesDao()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavLanguagesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_languages, parent, false)
        return FavLanguagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavLanguagesViewHolder, position: Int) {
        val subtype = list.get(position)
        //val subtypeDbModel = SubtypeDbModel(subtype.id)
        val subtypeId = subtype.id
        val titleText = subtype.locale.displayName
        val subtypeLayout = subtype.layout
        val summaryText = latestLocaleHelper.imeConfig.characterLayouts[subtypeLayout]
        holder.tvTitle.text = titleText
        holder.tvSummary.text = summaryText
        Log.d("FavAdapter:", "Code1: ${subtype.locale}")


        if (titleText == "English (United States)") {
            if (subtypesDao.getSubtypeById(subtypeId) == null) {
                Log.d("MyLogg:", "Adding english")
                val model =
                    LatestLanguageDbModel(
                        subtype.id
                    )
                subtypesDao.insertSingleSubtype(model)
            } else {
                Log.d("MyLogg:", "Not adding english")
            }
        }

        //todo change

        if (titleText == "Bulgarian (Bulgaria)") {
            if (subtypesDao.getSubtypeById(subtypeId) == null) {
                Log.d("AddLocale:", "Adding Bulgarian")
                val model =
                    LatestLanguageDbModel(
                        subtype.id
                    )
                subtypesDao.insertSingleSubtype(model)
            } else {
                Log.d("AddLocale:", "Not adding Bulgarian")
            }
        }

       /* if (titleText == "Bulgarian") {
            if (subtypesDao.getSubtypeById(subtypeId) == null) {
                Log.d("AddLocale:", "Adding Bulgarian")
                val model =
                    LatestLanguageDbModel(
                        subtype.id
                    )
                subtypesDao.insertSingleSubtype(model)
            } else {
                Log.d("AddLocale:", "Not adding Bulgarian")
            }
        }*/


        Glide.with(mContext).load(
            geFlagFromLocale(subtype.locale.toString())
        ).into(holder.ivFlag)
        // Log.d("LatestLangFrag:", "onBindViewHolder: ${subtype!!.locale.toString()}")
        holder.checkBox.isChecked = subtypesDao.getSubtypeById(subtypeId) != null

        holder.checkBox.setOnClickListener {
            if (titleText == "English (United States)") {
                Toast.makeText(
                    mContext,
                    "You cannot remove default language of keyboard.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                if (subtypesDao.getSubtypeById(subtypeId) != null) {
                    subtypesDao.deleteSubtypeById(subtypeId)
                    notifyDataSetChanged()
                } else {
                    try {
                        val model =
                            LatestLanguageDbModel(
                                subtype.id
                            )
                        subtypesDao.insertSingleSubtype(model)
                        notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        holder.itemView.setOnClickListener {
            if (titleText == "English (United States)") {
                Toast.makeText(
                    mContext,
                    "You cannot remove default language of keyboard.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                if (subtypesDao.getSubtypeById(subtypeId) != null) {
                    subtypesDao.deleteSubtypeById(subtypeId)
                    notifyDataSetChanged()
                } else {
                    try {
                        val model =
                            LatestLanguageDbModel(
                                subtype.id
                            )
                        subtypesDao.insertSingleSubtype(model)
                        notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        holder.checkBox.setOnClickListener {
            if (titleText == "Bulgarian (Bulgaria)") {
                Toast.makeText(
                    mContext,
                    "You cannot remove default language of keyboard.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                if (subtypesDao.getSubtypeById(subtypeId) != null) {
                    subtypesDao.deleteSubtypeById(subtypeId)
                    notifyDataSetChanged()
                } else {
                    try {
                        val model =
                            LatestLanguageDbModel(
                                subtype.id
                            )
                        subtypesDao.insertSingleSubtype(model)
                        notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        holder.itemView.setOnClickListener {
            if (titleText == "Bulgarian (Bulgaria)") {
                Toast.makeText(
                    mContext,
                    "You cannot remove default language of keyboard.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                if (subtypesDao.getSubtypeById(subtypeId) != null) {
                    subtypesDao.deleteSubtypeById(subtypeId)
                    notifyDataSetChanged()
                } else {
                    try {
                        val model =
                            LatestLanguageDbModel(
                                subtype.id
                            )
                        subtypesDao.insertSingleSubtype(model)
                        notifyDataSetChanged()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun geFlagFromLocale(locale: String): Int {
        var returner = R.drawable.ic_usa_flag
        for (model in listFlags) {
            if (model.flagCode == locale) {
                returner = model.flagIcon
            }
        }
        return returner
    }

    private fun checkForSavedItems(currentId: Int, holder: FavLanguagesViewHolder) {
        for (model in subtypesDao.allSubtypes) {
            if (currentId == model.id) {
                Log.d("DbLogger:", " Contains:")
                holder.checkBox.isChecked = true
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class FavLanguagesViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView
        var tvSummary: TextView
        var checkBox: CustomCheckBox
        var ivFlag: CircleImageView

        init {
            tvTitle = itemView.findViewById(R.id.tvTitle)
            tvSummary = itemView.findViewById(R.id.tvSummary)
            checkBox = itemView.findViewById(R.id.checkBox)
            ivFlag = itemView.findViewById(R.id.ivFlag)
        }
    }

}