package bzh.zelyon.music.ui.view.fragment.edit

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import bzh.zelyon.lib.extension.*
import bzh.zelyon.lib.ui.component.InputView
import bzh.zelyon.lib.ui.component.Popup
import bzh.zelyon.lib.ui.view.fragment.AbsToolBarFragment
import bzh.zelyon.lib.util.Launch
import bzh.zelyon.music.BuildConfig
import bzh.zelyon.music.R
import bzh.zelyon.music.db.model.AbsModel
import bzh.zelyon.music.ui.view.viewmodel.EditViewModel
import kotlinx.android.synthetic.main.fragment_edit.*
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File

abstract class AbsEditFragment<T: AbsModel>: AbsToolBarFragment() {

    lateinit var absModel: T
    private var currentArtwork: Drawable? = null
    protected var newArtwork: Artwork? = null
    protected var deleteCurrentArtwork = false
    protected var imageUrlFromLastFM: String? = null
    protected var infoFromLastFM: String? = null
        set(value) {
            field = value
            menu?.findItem(R.id.fragment_edit_info)?.isVisible = !value.isNullOrBlank()
        }
    private val inputViews = mutableListOf<InputView>()

    val editViewModel: EditViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        absModel = arguments?.getSerializable(ARG_ABS_MODEL) as T
        currentArtwork = (arguments?.getParcelable(ARG_ARTWORK) as? Bitmap)?.let {
            BitmapDrawable(absActivity.resources, it)
        } ?: absActivity.drawableResToDrawable(absModel.getPlaceholderId())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment_edit_toolbar.setPadding(0, absActivity.getStatusBarHeight(), 0, 0)
        fragment_edit_imageview_artwork.transitionName = absModel.getTransitionName()
        fragment_edit_imageview_artwork.setImage(absModel, currentArtwork)
        LayoutInflater.from(absActivity).inflate(getIdFormLayout(), fragment_edit_layout_form, true)
        getInputViews(view)
    }

    override fun getIdLayout() = R.layout.fragment_edit

    override fun getTitleToolBar() = absModel.getDeclaration()

    override fun getIdToolbar() = R.id.fragment_edit_toolbar

    override fun getIdMenu() = R.menu.fragment_edit

    override fun onIdClick(id: Int) {
        super.onIdClick(id)
        when (id) {
            R.id.fragment_edit_info -> Popup(absActivity, message = infoFromLastFM).showBottom()
            R.id.fragment_edit_save -> checkAndSave()
            R.id.fragment_edit_imageview_artwork -> onClickArtwork()
        }
    }

    abstract fun getIdFormLayout(): Int

    abstract fun onClickArtwork()

    abstract fun save()

    fun getImageOnDevice() {
        absActivity.launchWithResult(Launch.Get(false, Launch.Type.Image) { uris ->
            uris.firstOrNull()?.let { uri ->
                absActivity.getLocalFileFromGalleryUri(uri, absModel.getDeclaration() + ".png")?.let { file ->
                    fragment_edit_imageview_artwork.setImage(File(file.path), absActivity.drawableResToDrawable(absModel.getPlaceholderId()))
                    newArtwork = ArtworkFactory.createArtworkFromFile(file)
                    deleteCurrentArtwork = true
                }
            }
        })
    }

    fun getImageFromLastFM() {
        imageUrlFromLastFM?.let { imageUrlFromLastFM ->
            fragment_edit_imageview_artwork.setImage(imageUrlFromLastFM, absActivity.drawableResToDrawable(absModel.getPlaceholderId()))
            newArtwork = ArtworkFactory.createLinkedArtworkFromURL(imageUrlFromLastFM)
            deleteCurrentArtwork = true
        }
    }

    fun deleteArtwork() {
        fragment_edit_imageview_artwork.setImageResource(absModel.getPlaceholderId())
        deleteCurrentArtwork = true
    }

    private fun checkAndSave() {
        if (inputViews.all { it.checkValidity() }) {
            absActivity.launchPermissionFiles(BuildConfig.APPLICATION_ID) {
                if (it) {
                    save()
                } else {
                    Popup(absActivity,
                        title = getString(R.string.popup_permission_title),
                        message = getString(R.string.popup_permission_message),
                        positiveText = getString(R.string.popup_ok),
                        positiveClick = { checkAndSave() })
                        .show()
                }
            }
        }
    }

    private fun getInputViews(view: View) {
        if (view is InputView) {
            inputViews.add(view)
        } else if (view is ViewGroup) {
            view.children.forEach { getInputViews(it) }
        }
    }

    companion object {
        const val ARG_ABS_MODEL = "ARG_ABS_MODEL"
        const val ARG_ARTWORK = "ARG_ARTWORK"
    }
}