package com.tonopuchol.parking.parking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.chip.ChipGroup
import com.tonopuchol.parking.R
import com.tonopuchol.parking.ghent.ParkingData
import com.tonopuchol.parking.theme.green300
import com.tonopuchol.parking.theme.red300
import com.tonopuchol.parking.theme.yellow300
import com.tonopuchol.parking.utils.remove
import com.tonopuchol.parking.utils.toString

@Composable
fun ParkingContent(data: ParkingData, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = {
                val (icon, name, capacity, lastUpdated, description, remark, tags) = createRefs()
                val barrier = createTopBarrier(lastUpdated, tags)

                Icon(
                    painter = painterResource(id = R.drawable.ic_parking),
                    contentDescription = stringResource(R.string.parking_symbol),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .constrainAs(icon) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                        }
                )

                Text(
                    text = data.name ?: "",
                    modifier = Modifier.constrainAs(name) {
                        top.linkTo(parent.top)
                        start.linkTo(icon.end)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = data.description?.remove("? ") ?: "",
                    modifier = Modifier.constrainAs(description) {
                        top.linkTo(name.bottom)
                        start.linkTo(icon.end)
                        bottom.linkTo(barrier)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = with(AnnotatedString.Builder()) {
                        if (data.availableCapacity == 0) {
                            pushStyle(SpanStyle(color = red300, fontWeight = FontWeight.Bold))
                        }

                        append((data.availableCapacity ?: 0).toString())

                        append(" / ")
                        append((data.totalCapacity ?: 0).toString())

                        toAnnotatedString()
                    },
                    modifier = Modifier.constrainAs(capacity) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
                )

                Text(
                    text = data.lastUpdate?.toString("dd/MM/yyyy HH:mm:ss") ?: "",
                    fontSize = 10.sp,
                    modifier = Modifier.constrainAs(lastUpdated) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    modifier = Modifier
                        .constrainAs(tags) {
                            start.linkTo(parent.start)
                            bottom.linkTo(parent.bottom)
                        }
                        .padding(top = 4.dp),
                    content = {
                        if (data.category == "parking in LEZ") {
                            Surface(
                                modifier = Modifier.padding(end = 8.dp),
                                shadowElevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = green300
                                ),
                                content = {
                                    Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_eco),
                                            contentDescription = "LEZ"
                                        )
                                        Text(text = data.category)
                                    }
                                }
                            )
                        }
                    }
                )
            }
        )
    }
}

@Preview(name = "Parking Content", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ParkingContentPreview() {
    ParkingContent(ParkingData.getFilledForTest())
}