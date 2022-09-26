package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class MyGdxGame implements Screen {
	final Drop game;

	Texture primoImage;
	Texture bowlImage;
	Sound hitSound;
	Music backgroundMusic;
	OrthographicCamera camera;
	Rectangle bucket;
	Array<Rectangle> backgrounddrops;
	long lastDropTime;
	int dropsGathered;

	public MyGdxGame(final Drop gam) {
		this.game = gam;

		// загрузка изображений для капли и ведра, 64x64 пикселей каждый
		primoImage = new Texture(Gdx.files.internal("primo.png"));
		bowlImage = new Texture(Gdx.files.internal("bowl.png"));

		// загрузка звукового эффекта падающей капли и фоновой "музыки" дождя
		hitSound = Gdx.audio.newSound(Gdx.files.internal("hit.mp3"));
		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background.mp3"));
		backgroundMusic.setLooping(true);

		// создает камеру
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		// создается Rectangle для представления ведра
		bucket = new Rectangle();
		// центрируем ведро по горизонтали
		bucket.x = 800 / 2 - 64 / 2;
		// размещаем на 20 пикселей выше нижней границы экрана.
		bucket.y = 20;

		bucket.width = 64;
		bucket.height = 64;

		// создает массив капель и возрождает первую
		backgrounddrops = new Array<Rectangle>();
		spawnRaindrop();

	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800 - 64);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		backgrounddrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	@Override
	public void render(float delta) {
		// очищаем экран темно-синим цветом.
		// Аргументы для glClearColor красный, зеленый
		// синий и альфа компонент в диапазоне [0,1]
		// цвета используемого для очистки экрана.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// сообщает камере, что нужно обновить матрицы.
		camera.update();

		// сообщаем SpriteBatch о системе координат
		// визуализации указанных для камеры.
		game.batch.setProjectionMatrix(camera.combined);

		// начитаем новую серию, рисуем ведро и
		// все капли
		game.batch.begin();
		game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 480);
		game.batch.draw(bowlImage, bucket.x, bucket.y);
		for (Rectangle raindrop : backgrounddrops) {
			game.batch.draw(primoImage, raindrop.x, raindrop.y);
		}
		game.batch.end();

		// обработка пользовательского ввода
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
		}
		if (Gdx.input.isKeyPressed(Keys.LEFT))
			bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.RIGHT))
			bucket.x += 200 * Gdx.graphics.getDeltaTime();

		// убедитесь, что ведро остается в пределах экрана
		if (bucket.x < 0)
			bucket.x = 0;
		if (bucket.x > 800 - 64)
			bucket.x = 800 - 64;

		// проверка, нужно ли создавать новую каплю
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
			spawnRaindrop();

		// движение капли, удаляем все капли выходящие за границы экрана
		// или те, что попали в ведро. Воспроизведение звукового эффекта
		// при попадании.
		Iterator<Rectangle> iter = backgrounddrops.iterator();
		while (iter.hasNext()) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0)
				iter.remove();
			if (raindrop.overlaps(bucket)) {
				dropsGathered++;
				hitSound.play();
				iter.remove();
			}
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		// воспроизведение фоновой музыки
		// когда отображается экрана
		backgroundMusic.play();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		primoImage.dispose();
		bowlImage.dispose();
		hitSound.dispose();
		backgroundMusic.dispose();
	}

}
