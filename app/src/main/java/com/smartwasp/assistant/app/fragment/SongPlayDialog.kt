package com.smartwasp.assistant.app.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.smartwasp.assistant.app.R
import com.smartwasp.assistant.app.bean.SongBean
import com.smartwasp.assistant.app.util.IFLYOS
import kotlinx.android.synthetic.main.fragment_bottom_song.*

/**
 * Created by luotao on 2021/1/21 13:36
 * E-Mail Address：gtkrockets@163.com
 */
class SongPlayDialog private constructor(): BottomSheetDialogFragment() {
    companion object{
        fun newsInstance(song: SongBean):SongPlayDialog{
            val songPlayDialog = SongPlayDialog()
            songPlayDialog.arguments = Bundle().apply {
                putSerializable(IFLYOS.EXTRA,song)
            }
            return songPlayDialog
        }
    }

    /**
     * 生成
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CommonWindowStyle_BottomSheet)
    }

    /**
     * 产生布局
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_song,container)
    }

    /**
     * 视图呈现
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (arguments?.getSerializable(IFLYOS.EXTRA) as SongBean?)?.let {
            sheet_play_tip.text = it.name
        }
        sheet_cancel_btn.setOnClickListener {
            dismissAllowingStateLoss()
        }
        //todo 临时写法
        sheet_play_btn.setOnClickListener {
            (requireArguments()[IFLYOS.EXTRA] as SongBean?)?.let {song->
                it.setTag(R.id.extra_tag,song)
                (parentFragment as? MusicItemFragment?)?.onButtonClick(it)
            }
            dismissAllowingStateLoss()
        }
    }
}