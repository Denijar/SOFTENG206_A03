package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;

import java.io.File;
import java.util.ArrayList;

/**
 * The task that creations a Creation
 * Periodically updates its progress status for the ProgressBar
 */
public class createCreationJob extends Task<Void> {

    private  String blankText = "";
    private String _searchTerm;
    private ArrayList<File> images;

    public createCreationJob(String searchTerm, ArrayList<File> images){
        _searchTerm = searchTerm;
        this.images = images;
    }

    @Override
    protected Void call() throws Exception {

        //get number of images
        File file = new File("images_to_use");
        int _numberOfImages = file.listFiles().length;
        //calculate duration for each image in slideshow, given audio duration
        Command command = new Command("soxi -D .temp_audio.wav");
        command.execute();

        updateProgress(3,10);
        double duration = Double.parseDouble(command.getStream());
        Double framerate = (_numberOfImages/duration);
        updateProgress(4,10);
        //make the video
        //TODO: change the output resolution to be the same as the new video player
        updateProgress(5,10);
        command = new Command("cat images_to_use" + System.getProperty("file.separator") + "*.jpg | ffmpeg -f image2pipe -framerate " + framerate + " -i - -vf \"scale=710:504, drawtext=fontsize=50:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:shadowcolor=black:shadowx=2:shadowy=2:text=" + _searchTerm + "\" -r 25 -y .temp_video.mp4");
        command.execute();
        updateProgress(6,10);
        command = new Command("ffmpeg -f lavfi -i color=c=black:s=320x240:d=5 -vf \"drawtext=fontfile=myfont.ttf:fontsize=30: fontcolor=black:x=(w-text_w)/2:y=(h-text_h)/2:text='awesome'\" .blankVideo.mp4\n");
        command.execute();

        //TODO: check that this globbing method of video creation is working (it might be missing the last two images)
        //TODO: it also must match the resolution of the Quiz player that we use
        command = new Command("ffmpeg -framerate " + framerate + " -pattern_type glob -i 'images_to_use/*.jpg' -vf \"scale=414:312, drawtext=fontfile=fonts/myfont.ttf:fontsize=100: fontcolor=black:x=(w-text_w)/2:y=(h-text_h)/2:text=" + blankText + "\" .noTextVideo.mp4");
        command.execute();
        updateProgress(7,10);
        command = new Command("ffmpeg -y -i .temp_audio.wav -i .temp_video.mp4 -c:v copy -c:a aac -strict experimental final_creation.mp4");
        command.execute();
        updateProgress(8,10);
        command = new Command("ffmpeg -y -i .temp_audio.wav -i .blankVideo.mp4 -c:v copy -c:a aac -strict experimental quiz1.mp4");
        command.execute();
        updateProgress(9,10);
        command = new Command("ffmpeg -y -i .temp_audio.wav -i .noTextVideo.mp4 -c:v copy -c:a aac -strict experimental quiz2.mp4");
        command.execute();
        updateProgress(10,10);

        return null;
    }
}
