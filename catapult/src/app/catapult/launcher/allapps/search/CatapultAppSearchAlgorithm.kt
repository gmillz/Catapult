/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.catapult.launcher.allapps.search

import android.content.Context
import android.os.Handler
import androidx.annotation.AnyThread
import com.android.launcher3.LauncherAppState
import com.android.launcher3.allapps.BaseAllAppsAdapter
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem
import com.android.launcher3.model.AllAppsList
import com.android.launcher3.model.BaseModelUpdateTask
import com.android.launcher3.model.BgDataModel
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.search.SearchAlgorithm
import com.android.launcher3.search.SearchCallback
import com.android.launcher3.search.StringMatcherUtility
import com.android.launcher3.util.Executors
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio
import java.util.Locale

/**
 * The default search implementation.
 */
class CatapultAppSearchAlgorithm @JvmOverloads constructor(
    context: Context?,
    addNoResultsMessage: Boolean = false
) : SearchAlgorithm<AdapterItem?> {
    private val mAppState: LauncherAppState
    private val mResultHandler: Handler
    private val mAddNoResultsMessage: Boolean

    init {
        mAppState = LauncherAppState.getInstance(context)
        mResultHandler = Handler(Executors.MAIN_EXECUTOR.looper)
        mAddNoResultsMessage = addNoResultsMessage
    }

    override fun cancel(interruptActiveRequests: Boolean) {
        if (interruptActiveRequests) {
            mResultHandler.removeCallbacksAndMessages(null)
        }
    }

    override fun doSearch(query: String, callback: SearchCallback<AdapterItem?>?) {
        mAppState.model.enqueueModelUpdateTask(object : BaseModelUpdateTask() {
            override fun execute(
                app: LauncherAppState,
                dataModel: BgDataModel, apps: AllAppsList
            ) {
                val result: ArrayList<AdapterItem?> =
                    getTitleMatchResult(
                        apps.data, query
                    )
                if (mAddNoResultsMessage && result.isEmpty()) {
                    result.add(
                        getEmptyMessageAdapterItem(
                            query
                        )
                    )
                }
                mResultHandler.post { callback?.onSearchResult(query, result) }
            }
        })
    }

    companion object {
        private const val MAX_RESULTS_COUNT = 5
        private fun getEmptyMessageAdapterItem(query: String): AdapterItem {
            val item = AdapterItem(BaseAllAppsAdapter.VIEW_TYPE_EMPTY_SEARCH)
            // Add a place holder info to propagate the query
            val placeHolder = AppInfo()
            placeHolder.title = query
            item.itemInfo = placeHolder
            return item
        }

        /**
         * Filters [AppInfo]s matching specified query
         */
        @AnyThread
        fun getTitleMatchResult(apps: List<AppInfo>, query: String): ArrayList<AdapterItem?> {
            // Do an intersection of the words in the query and each title, and filter out all the
            // apps that don't match all of the words in the query.
            val queryTextLower = query.lowercase(Locale.getDefault())
            val matches = FuzzySearch.extractSorted(queryTextLower, apps,
                                                    { it.sectionName + it.title},
                                                    WeightedRatio(), 65)
            return ArrayList(matches.map { AdapterItem.asApp(it.referent) })
        }
    }
}