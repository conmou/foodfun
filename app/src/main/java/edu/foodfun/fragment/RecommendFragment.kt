package edu.foodfun.fragment

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lin.cardlib.CardLayoutManager
import com.lin.cardlib.CardTouchHelperCallback
import com.lin.cardlib.OnSwipeCardListener
import com.lin.cardlib.utils.ReItemTouchHelper
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.RecommendCardSetting
import edu.foodfun.activity.MainActivity
import edu.foodfun.adapter.CommentRecyclerViewAdapter
import edu.foodfun.adapter.RecommendCardRecyclerViewAdapter
import edu.foodfun.model.Comment
import edu.foodfun.uistate.CommentUIState
import edu.foodfun.uistate.RestaurantUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecommendFragment : Fragment() {
    private lateinit var recyclerRecommendCardList: RecyclerView
    private lateinit var recyclerCommentList: RecyclerView
    private val vm: MainViewModel by activityViewModels()
    private var recommendRestList: MutableList<RestaurantUIState> = mutableListOf()
    private var commentUIStateList: MutableList<CommentUIState> = mutableListOf()
    private var commentList: MutableList<Comment> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_recommend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerRecommendCardList = view.findViewById(R.id.recyclerRecommendCardList)
        recyclerCommentList = view.findViewById(R.id.recyclerCommentList)
        recyclerCommentList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val space = 20
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = space
                outRect.right = space
                outRect.bottom = space
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top = space
                }
            }
        })

        val cardSetting = RecommendCardSetting()
        cardSetting.setSwipeListener(object : OnSwipeCardListener<RestaurantUIState> {
            override fun onSwiping(holder: RecyclerView.ViewHolder?, dx: Float, dy: Float, direction: Int) {
                //當正在滑動時可以做一些狀態改變
            }
            @SuppressLint("NotifyDataSetChanged")
            override fun onSwipedOut(holder: RecyclerView.ViewHolder?, data: RestaurantUIState, direction: Int) {
                if (direction == ReItemTouchHelper.DOWN) {
                    //
                }
                else if (direction == ReItemTouchHelper.LEFT) {
                    //
                }
                else if (direction == ReItemTouchHelper.RIGHT) {
                    lifecycleScope.launch {
                        vm.addRestaurantToDefaultGroup(data.restaurant.id!!)
                        vm.addRecommendParams(data.restaurant.avgCost, data.restaurant.location!!, data.restaurant.type)
                    }
                }

                commentList.clear()
                if (commentUIStateList.size > 0) commentUIStateList.removeAt(0) //移除被滑掉之餐廳的留言集合
                if (commentUIStateList.size > 0) commentList.addAll(commentUIStateList[0].comments) //加入餐廳新留言集合
                recyclerCommentList.adapter?.notifyDataSetChanged()
                recyclerCommentList.smoothScrollToPosition(0)
            }
            override fun onSwipedClear() {
                lifecycleScope.launch{ fetchRestaurants() }
            }
        })
        val helperCallback = CardTouchHelperCallback(recyclerRecommendCardList, recommendRestList, cardSetting)
        val itemTouchHelper = ReItemTouchHelper(helperCallback)

        recyclerRecommendCardList.adapter = RecommendCardRecyclerViewAdapter(recommendRestList)
        recyclerRecommendCardList.layoutManager = CardLayoutManager(itemTouchHelper, cardSetting)

        recyclerCommentList.adapter = CommentRecyclerViewAdapter(commentList)
        recyclerCommentList.layoutManager = LinearLayoutManager(context)

        launchFlow()
    }

    private fun launchFlow() {
        lifecycleScope.launch { fetchRestaurants() }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun fetchRestaurants() {
        if (recommendRestList.size > 0) return
        vm.fetchRecommendRestaurants.collect {
            if (recommendRestList.size == 0) { //當載入第一間餐廳時 需顯示該餐廳之留言
                commentUIStateList = mutableListOf()
                commentList.clear()
                commentList.addAll(it.commentUIState.comments)
                recyclerCommentList.adapter?.notifyDataSetChanged()
            }
            recommendRestList.add(it.restaurantUIState)
            commentUIStateList.add(it.commentUIState)
            val adapter = recyclerRecommendCardList.adapter as RecommendCardRecyclerViewAdapter
            adapter.notifyItemInserted(recommendRestList.size)
        }
    }
    /*private fun touchListener(){
        val detector = GestureDetector(this@RecommendFragment, object : GestureDetector.OnGestureListener{
            override fun onFling(
                p0: MotionEvent?,
                p1: MotionEvent?,
                p2: Float,
                p3: Float
            ): Boolean {
                return false
            }
        })
        https://blog.csdn.net/qq_34589749/article/details/105810226
        https://blog.csdn.net/qq_28316949/article/details/57121666
    }*/
}