# Dice similarity function
import keras.backend as K
import tensorflow as tf
from skimage import io, transform
import cv2
import numpy as np


def dice_coef(y_true, y_pred, smooth=100):        
    y_true_f = K.flatten(y_true)
    y_pred_f = K.flatten(y_pred)
    intersection = K.sum(y_true_f * y_pred_f)
    dice = (2. * intersection + smooth) / (K.sum(y_true_f) + K.sum(y_pred_f) + smooth)
    return dice

def run_model(imagePath=""):
    dependencies = {'dice_coef': dice_coef}
    modell = tf.keras.models.load_model('shitty_model.h5', custom_objects=dependencies)
    modell.summary()

    if imagePath == "":
        path_im =r"C:\Users\Kasia\Documents\GitHub\nails_segmentation\zdjecie_do_sklasyfikowania.jpg"
    else:
        path_im=imagePath
# load the input image
    img = cv2.imread(path_im)

    # preprocess the image
    img = cv2.resize(img, (800, 800))
    img = img.astype(np.float32) / 255.0
    img = np.expand_dims(img, axis=0)

    # run the model to make a prediction
    output = modell.predict(img)

    # postprocess the output image
    output = output.squeeze()
    output = np.clip(output, 0.0, 1.0)
    output = output * 255.0
    output = output.astype(np.uint8)

    # convert the output image to a byte array for display in the app
    _, buffer = cv2.imencode('.png', output)
    return buffer.tobytes()