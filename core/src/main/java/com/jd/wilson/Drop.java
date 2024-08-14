package com.jd.wilson;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.SortedIntList;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {
    private Texture dropImage;
    private Texture bucketImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Rectangle bucket;
    private Vector3 touchPos;
    private Array<Rectangle> raindrops;
    private long lastDropTime; // time in nanoseconds

    @Override
    public void create() {
        // load the images for the droplet and bucket. 64x64 pixels each
        dropImage = new Texture(Gdx.files.internal("ui/drop.png"));
        bucketImage = new Texture(Gdx.files.internal("ui/bucket.png"));

        // load the drop sound effect and the rain background "music"
        // use 'sound' for audio less than 10 second. This gets stored in memory
        // use 'music' for longer audio. This is streamed directly from where it's stored
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // start playing the background music immediately
        rainMusic.setLooping(true);
        rainMusic.play();

        // camera is what the user sees
        // virtual window into the world we're creating
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        batch = new SpriteBatch();

        touchPos = new Vector3();

        // center the bucket horizontally and place it 20 pixels above the bottom egde of the screen
        bucket = new Rectangle();
        bucket.x = (800 / 2) - (64 / 2);
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;

        raindrops = new Array<>();
        spawnRaindrop();
    }

    @Override
    public void render() {
        // set screen to dark blue
        ScreenUtils.clear(0, 0, 0.2f, 1);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle raindrop: raindrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }
        batch.end();

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - (64 / 2);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucket.x += 200 * Gdx.graphics.getDeltaTime();
        }
        if (bucket.x < 0) {
            bucket.x = 0;
        }
        if (bucket.x > 800 - 64) {
            bucket.x = 800 - 64;
        }
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000){
            spawnRaindrop();
        }

        for (Iterator<Rectangle> iterator = raindrops.iterator(); iterator.hasNext();) {
            Rectangle raindrop = iterator.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();

            if (raindrop.y + 64 < 0) {
                iterator.remove();
            }

            if (raindrop.overlaps(bucket)) {
                dropSound.play();
                iterator.remove();
            }
        }
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 800-64);
        raindrop.y = 480;
        raindrop.width = 64;
        raindrop.height = 64;
        raindrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void dispose() {
        dropImage.dispose();
        dropSound.dispose();
        bucketImage.dispose();
        rainMusic.dispose();
        batch.dispose();
    }
}
