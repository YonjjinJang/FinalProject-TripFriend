package com.test.tripfriend.ui.trip

import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.test.tripfriend.ui.main.MainActivity
import com.test.tripfriend.R
import com.test.tripfriend.databinding.DialogSubmitBinding
import com.test.tripfriend.databinding.FragmentReadPostBinding
import com.test.tripfriend.repository.TripPostRepository
import com.test.tripfriend.viewmodel.TripPostViewModel
import com.test.tripfriend.viewmodel.UserViewModel
import kotlin.concurrent.thread

class ReadPostFragment : Fragment() {
    lateinit var fragmentReadPostBinding : FragmentReadPostBinding
    lateinit var mainActivity: MainActivity

    lateinit var tripPostViewModel: TripPostViewModel
    lateinit var userViewModel: UserViewModel

    val tripPostRepository = TripPostRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        fragmentReadPostBinding = FragmentReadPostBinding.inflate(layoutInflater)

        // 이전 화면에서 데이터 가져오기
        val tripPostWriterEmail = arguments?.getString("tripPostWriterEmail")!!
        val tripPostDocumentId = arguments?.getString("tripPostDocumentId")!!
        val viewState = arguments?.getString("viewState")

        val newBundle = Bundle()

        tripPostViewModel = ViewModelProvider(this)[TripPostViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        tripPostViewModel.tripPostList.observe(viewLifecycleOwner) { tripPost ->
            fragmentReadPostBinding.run {
                textViewReadPostTitle.text = tripPost.tripPostTitle

                textViewReadPostDate.text = "${tripPost.tripPostDate?.get(0)} ~ ${tripPost.tripPostDate?.get(1)}"

                textViewReadPostNOP.text = tripPost.tripPostMemberCount.toString()

                textViewReadPostLocatoin.text = tripPost.tripPostLocationName

                when(tripPost.tripPostTripCategory?.size) {
                    1 -> {
                        chipReadPostCategory1.text = tripPost.tripPostTripCategory[0]
                        chipReadPostCategory1.chipIcon = chipIcon(tripPost.tripPostTripCategory[0])
                        chipReadPostCategory2.visibility = View.INVISIBLE
                        chipReadPostCategory3.visibility = View.INVISIBLE
                    }
                    2 -> {
                        chipReadPostCategory1.text = tripPost.tripPostTripCategory[0]
                        chipReadPostCategory1.chipIcon = chipIcon(tripPost.tripPostTripCategory[0])
                        chipReadPostCategory2.text = tripPost.tripPostTripCategory[1]
                        chipReadPostCategory2.chipIcon = chipIcon(tripPost.tripPostTripCategory[1])
                        chipReadPostCategory3.visibility = View.INVISIBLE
                    }
                    3 -> {
                        chipReadPostCategory1.text = tripPost.tripPostTripCategory[0]
                        chipReadPostCategory1.chipIcon = chipIcon(tripPost.tripPostTripCategory[0])
                        chipReadPostCategory2.text = tripPost.tripPostTripCategory[1]
                        chipReadPostCategory2.chipIcon = chipIcon(tripPost.tripPostTripCategory[1])
                        chipReadPostCategory3.text = tripPost.tripPostTripCategory[2]
                        chipReadPostCategory3.chipIcon = chipIcon(tripPost.tripPostTripCategory[2])
                    }
                    else -> {
                        chipReadPostCategory1.visibility = View.INVISIBLE
                        chipReadPostCategory2.visibility = View.INVISIBLE
                        chipReadPostCategory3.visibility = View.INVISIBLE
                    }
                }

                textViewReadPostHashTag.text = tripPost.tripPostHashTag

                textViewReadPostContent.text = tripPost.tripPostContent

                newBundle.putString("tripPostDocumentId", tripPost.tripPostDocumentId)
                newBundle.putStringArrayList("tripPostMemberList", tripPost.tripPostMemberList as ArrayList<String>?)
                newBundle.putString("tripPostTitle", tripPost.tripPostTitle)
                newBundle.putString("tripPostWriterEmail", tripPost.tripPostWriterEmail)

                if(tripPost.tripPostImage!!.isNotEmpty()) {
                    tripPostViewModel.getTargetUserProfileImage(tripPost.tripPostImage)
                }
            }
        }

        tripPostViewModel.tripPostImage.observe(viewLifecycleOwner) { uri ->
            if(uri != null) {
                Glide.with(mainActivity).load(uri)
                    .error(R.drawable.login_background_image)
                    .into(fragmentReadPostBinding.imageViewReadPostMainImage)
            } else {
                fragmentReadPostBinding.imageViewReadPostMainImage.setImageResource(R.drawable.login_background_image)
            }
        }

        tripPostViewModel.getSelectDocumentData(tripPostDocumentId)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            fragmentReadPostBinding.run {
                textViewUserNickname.text = user.userNickname

                textViewUserMBTI.text = user.userMBTI

                if(user.userProfilePath.isNotEmpty() ) {
                    userViewModel.getTargetUserProfileImage(user.userProfilePath)
                }
            }
        }

        userViewModel.userProfileImage.observe(viewLifecycleOwner) { uri ->
            if(uri != null) {
                Glide.with(mainActivity).load(uri)
                    .error(R.drawable.person_24px)
                    .into(fragmentReadPostBinding.imageViewUserProfileImage)
            } else {
                fragmentReadPostBinding.imageViewReadPostMainImage.setImageResource(R.drawable.person_24px)
            }
        }

        userViewModel.getTargetUserData(tripPostWriterEmail, "이메일")

        mainActivity.activityMainBinding.bottomNavigationViewMain.visibility = View.GONE

        fragmentReadPostBinding.run {
            materialToolbarReadPost.run {
                setNavigationIcon(R.drawable.arrow_back_24px)
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.READ_POST_FRAGMENT)
                }

                // 이전 화면에 따라 visibility 처리
                when(viewState) {
                    "InProgressFragment" -> { // 참여중인 동행
                        buttonReadPostDM.visibility = View.GONE
                        buttonReadPostSubmit.visibility = View.GONE
                        buttonReadPostMoveChat.visibility = View.VISIBLE
                        buttonReadPostReview.visibility = View.GONE

                    }
                    "PassFragment" -> { // 지난 동행
                        buttonReadPostDM.visibility = View.GONE
                        buttonReadPostSubmit.visibility = View.GONE
                        buttonReadPostMoveChat.visibility = View.GONE
                        buttonReadPostReview.visibility = View.VISIBLE
                        
                        // 메뉴를 숨기려면
                        var toolbar = findViewById<MaterialToolbar>(R.id.materialToolbarReadPost)
                        toolbar.menu.findItem(R.id.menu_item_delete).isVisible = false
                    }
                }

//                // 메뉴를 보이게 하려면
//                var toolbar = findViewById<MaterialToolbar>(R.id.materialToolbarReadPost)
//                toolbar.menu.findItem(R.id.menu_item_modify).isVisible = true

                setOnMenuItemClickListener {
                    when(it.itemId){
                        //삭제
                        R.id.menu_item_delete->{
                            val builder = MaterialAlertDialogBuilder(mainActivity, R.style.DialogTheme)
                            builder.run {
                                setTitle("게시글 삭제")
                                setMessage("게시글을 삭제하시면 관련된 정보 및 그룹채팅이 삭제 됩니다.")
                                setPositiveButton("삭제") { dialogInterface: DialogInterface, i: Int ->
                                    tripPostRepository.deleteTripPostData(tripPostDocumentId)
                                    mainActivity.removeFragment(MainActivity.READ_POST_FRAGMENT)
                                }
                                setNegativeButton("취소", null)
                                show()
                            }
                        }
                        //수정
                        R.id.menu_item_modify ->{
                            mainActivity.replaceFragment(MainActivity.MODFY_POST_FRAGMENT,true,true, newBundle)
                        }
                    }
                    true
                }
            }

            // 프로필 보기 버튼 -> 내정보 동행리뷰로 전환
            textViewShowProfile.run{
                isClickable = true
                setOnClickListener {
                    mainActivity.replaceFragment(MainActivity.MY_ACCOMPANY_INFO_FRAGMENT, true, true, newBundle)
                }
            }

            //DM버튼
            buttonReadPostDM.setOnClickListener {
                //다이얼로그 띄움
                val builder = MaterialAlertDialogBuilder(mainActivity, R.style.DialogTheme)
                builder.run {
                    setTitle("1 : 1 문의하기")
                    setMessage(R.string.DM_info)
                    setPositiveButton("입장") { dialogInterface: DialogInterface, i: Int ->
                        mainActivity.replaceFragment(MainActivity.PERSONAL_CHAT_ROOM_FRAGMENT, true, true, newBundle)
                    }
                    setNegativeButton("취소", null)
                    show()
                }
            }

            //동행 신청 버튼
            buttonReadPostSubmit.setOnClickListener {
                //다이얼로그 띄움
                val builder = MaterialAlertDialogBuilder(mainActivity, R.style.DialogTheme)
                builder.run {
                    val dialogBinding = DialogSubmitBinding.inflate(layoutInflater)
                    setTitle("동행 신청하기")
                    setMessage("동행 신청을 위한 자기소개를 해주세요")

                    // 새로운 뷰를 설정한다.
                    builder.setView(dialogBinding.root)

                    dialogBinding.editTextInputSelfIntroduce.requestFocus()

                    thread {
                        SystemClock.sleep(500)
                        val imm = mainActivity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(dialogBinding.editTextInputSelfIntroduce, 0)
                    }

                    builder.setPositiveButton("신청") { dialogInterface: DialogInterface, i: Int ->
                        // 입력한 내용을 가져온다.
                        val str1 = dialogBinding.editTextInputSelfIntroduce.text.toString()
                        textViewReadPostToolbarTitle.text = str1
                    }
                    builder.setNegativeButton("취소", null)

                    builder.show()
                }
            }

            //그룹 채팅으로 이동 버튼
            buttonReadPostMoveChat.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.GROUP_CHAT_ROOM_FRAGMENT,true,true, newBundle)
            }

            //리뷰 버튼
            buttonReadPostReview.setOnClickListener {
                mainActivity.replaceFragment(MainActivity.REVIEW_FRAGMENT,true,true, newBundle)
            }
        }

        return fragmentReadPostBinding.root
    }

    override fun onPause() {
        super.onPause()
        mainActivity.activityMainBinding.bottomNavigationViewMain.visibility = View.VISIBLE
    }

    // chip 아이콘
    fun chipIcon(chipCategory: String): Drawable? {
        var drawable: Drawable? = null
        when(chipCategory) {
            "맛집 탐방" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.restaurant_20px)
            }
            "휴양" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.breaktime_20px)
            }
            "관광" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.tour_20px)
            }
            "축제" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.festival_20px)
            }
            "자연" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.forest_20px)
            }
            "쇼핑" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.shopping_bag_20px)
            }
            "액티비티" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.activity_20px)
            }
            "사진촬영" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.photo_camera_20px)
            }
            "스포츠" -> {
                drawable = ContextCompat.getDrawable(mainActivity, R.drawable.sports_soccer_20px)
            }
        }
        return drawable
    }
}

