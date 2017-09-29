package temedia;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author igor Last edited 29.09.2017
 */

public class FXMLDocumentController implements Initializable {

    private MediaPlayer mplayer;
    private String filePath;

    @FXML
    private MediaView mediaView;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label lblVolume;

    @FXML
    private Slider seekSlider;

    @FXML
    private Label lblDuration;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                "Select a file", "*.mp4", "*.avi", "*.flv", "*.mp3");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            filePath = file.toURI().toString();
            Media media = new Media(filePath);
            String title = filePath.replace("%20", " ").replace("file:", "");
            Stage stage = (Stage) mediaView.getScene().getWindow();
            stage.setTitle(title);
            if (mplayer != null && mplayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mplayer.stop();
                mplayer.dispose();
            }
            mplayer = new MediaPlayer(media);

            mediaView.setMediaPlayer(mplayer);
            seekSlider.setMin(0);
            seekSlider.setMax(100);
            DoubleProperty width = mediaView.fitWidthProperty();
            DoubleProperty height = mediaView.fitHeightProperty();
            width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
            height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));

            mplayer.setOnReady(() -> {
                seekSlider.setMax(mplayer.getTotalDuration().toSeconds());

                seekSlider.valueProperty().addListener((p, o, value) -> {
                    if (seekSlider.isPressed()) {
                        mplayer.seek(Duration.seconds(value.doubleValue()));
                    }
                });

                mplayer.currentTimeProperty().addListener((p, o, value) -> {
                    double current = value.toSeconds();
                    String txtCurrent = durationToString(value);
                    double total = mplayer.getTotalDuration().toSeconds();
                    String txtTotal = durationToString(mplayer.getTotalDuration());
                    double remains = total - current;
                    String txtRemains = durationToString(Duration.seconds(remains));
                    seekSlider.setValue(current);
                    lblDuration.setText(String.format("%s/%s(%s)",
                            txtCurrent, txtTotal, txtRemains));
                });
            });

            volumeSlider.setValue(mplayer.getVolume() * 100);
            lblVolume.setText("100%");
            lblDuration.setText(mplayer.getTotalDuration().toString());
            volumeSlider.valueProperty().addListener(e -> {
                mplayer.setVolume(volumeSlider.getValue() / 100);
                lblVolume.setText(String.format("%5.0f",
                        volumeSlider.getValue()) + "%");
            });

            mediaView.getScene().setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    Stage s = (Stage) mediaView.getScene().getWindow();
                    s.setFullScreen(!stage.isFullScreen());
                }
            });
            
            mediaView.getScene().setOnScroll(e -> {
                //Volume
                if(e.getDeltaY() > 0){
                    volumeSlider.setValue(volumeSlider.getValue() + 1);
                }
                else if(e.getDeltaY() < 0){
                    volumeSlider.setValue(volumeSlider.getValue() - 1);
                }
                //Seeking
                if(e.getDeltaX() > 0){
                    mplayer.seek(Duration.seconds(mplayer.getCurrentTime()
                            .toSeconds() + 30));
                }
                else if(e.getDeltaX() < 0){
                    mplayer.seek(Duration.seconds(mplayer.getCurrentTime()
                            .toSeconds() - 30));
                }
            });

            mediaView.getScene().setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.P) {
                    if (mplayer.getStatus() == MediaPlayer.Status.PAUSED) {
                        mplayer.play();
                    } else if (mplayer.getStatus() == MediaPlayer.Status.PLAYING) {
                        mplayer.pause();
                    }
                }
            });

            mplayer.play();
        }
    }
    
    String durationToString(Duration value){
        int hour = (int)value.toHours();
        int min = (int)value.toMinutes() - hour * 60;
        int sec = (int)value.toSeconds() - hour * 3600 - min * 60;
        String res = String.format("%d:%d:%d", hour, min, sec);
        return res;
    }

    @FXML
    private void playVideo(ActionEvent event) {
        mplayer.play();
    }

    @FXML
    private void pauseVideo(ActionEvent event) {
        mplayer.pause();
    }

    @FXML
    private void stopVideo(ActionEvent event) {
        mplayer.stop();
        mplayer.dispose();
    }

    @FXML
    private void slowerVideo(ActionEvent event) {
        mplayer.setRate(mplayer.getRate() * 1 / 1.5);
    }

    @FXML
    private void setNormalVideoSpeed(ActionEvent event) {
        mplayer.setRate(1);
    }

    @FXML
    private void fasterVideo(ActionEvent event) {
        mplayer.setRate(mplayer.getRate() * 1.5);
    }

    @FXML
    private void seekForward(ActionEvent event) {
        mplayer.seek(Duration.seconds(mplayer.getCurrentTime().toSeconds() + 10));
    }

    @FXML
    private void seekBackward(ActionEvent event) {
        mplayer.seek(Duration.seconds(mplayer.getCurrentTime().toSeconds() - 10));
    }

    @FXML
    private void exitVideo(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblVolume.setText("0%");
    }

}
