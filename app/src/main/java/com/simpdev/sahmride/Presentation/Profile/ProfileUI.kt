
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun ProfileUi(
    profileImageBitmap:ImageBitmap,
    firstName:String,
    lastName:String,
    gender:String,
){
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Image(
                modifier = Modifier
                    .zIndex(1f)
                    .width(100.dp)
                    .height(100.dp)
                    .clip(shape = CircleShape),
                contentScale = ContentScale.Crop,
                bitmap = profileImageBitmap,
                contentDescription = "",
            )
            Column {
                Text(text = firstName + " " +lastName)
                Row() {
                    Icon(imageVector = if(gender == "Male") Icons.Filled.Male else Icons.Filled.Female, contentDescription = null)
                    Text(text = gender)
                }
            }
        }

    }
}