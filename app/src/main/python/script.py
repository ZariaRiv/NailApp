import tensorflow as tf
import numpy as np
import cv2
import os


def see_model(image_path):
    path= os.path.abspath('shitty_model.h5')
    return path

def run_model(image_path):
# get the absolute path to the current directory
    cwd = os.getcwd()

    # load the trained model
    model_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'chat_model.h5')
    model = tf.keras.models.load_model(model_path)

    # load the input image
    img = cv2.imread(image_path)

    # preprocess the image
    img = cv2.resize(img, (800, 800))
    img = img.astype(np.float32) / 255.0
    img = np.expand_dims(img, axis=0)

    # run the model to make a prediction
    output = model.predict(img)

    # postprocess the output image
    output = output.squeeze()
    output = np.clip(output, 0.0, 1.0)
    output = output * 255.0
    output = output.astype(np.uint8)

    # convert the output image to a byte array for display in the app
    _, buffer = cv2.imencode('.png', output)
    return buffer.tobytes()
