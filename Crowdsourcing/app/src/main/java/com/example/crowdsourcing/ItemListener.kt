/**
 * Wipu     Kumthong            6088095
 * Pada     Kanchanapinpong     6088079
 * Thanirin Trironnarith        6088122
 *
 * ItemListener.kt passes fun "onClicked" from Adapters to other classes
 */


import com.mikhaellopez.circularimageview.CircularImageView

/**
 * pass fun "onClicked" from Adapters to other classes
 */
interface ItemListener {
    fun onClicked(imageView: CircularImageView, textView: String, username: String, img: String)
}