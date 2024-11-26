// src/main/java/app/view/ImageViewWrapper.java

package app.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Класс ImageViewWrapper упрощает загрузку и настройку ImageView с заданным ресурсом и размерами.
 */
public class ImageViewWrapper {

    private final ImageView imageView;

    /**
     * Конструктор класса ImageViewWrapper.
     *
     * @param resourcePath путь к ресурсу изображения
     * @param width        ширина ImageView
     * @param height       высота ImageView
     */
    public ImageViewWrapper(String resourcePath, double width, double height) {
        Image image = new Image(getClass().getResourceAsStream(resourcePath));
        if (image.isError()) {
            System.err.println("Не удалось загрузить изображение по пути: " + resourcePath);
            // Вы можете выбросить исключение или обработать ошибку по-другому
        }
        imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
    }

    /**
     * Возвращает ImageView.
     *
     * @return ImageView с загруженным изображением
     */
    public ImageView getImageView() {
        return imageView;
    }
}