package data;


public class main_data {
    private int image;
    private String name, message;

    public main_data(int image, String name, String message) {
        this.image = image;
        this.name = name;
        this.message = message;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
