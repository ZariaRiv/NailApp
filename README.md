# NailApp
App for taking pictures and making nail segmentation on a photo

On this point in the project there is no database to calculate the hemoglobin level, however an algorithm for nail detection was created. I believe that the algorithm can be useful later, if not on its own, then at least to help build the bigger model that will calculate the hemoglobin level as it will need first to find the fingernails. 

I made a Deep Learning model that was supposed to detect nails in the photo, due to limited dataset available on the internet the model is not highly accurate, however I believe with the lareder dataset obtained later in the project the predictions are promising. The model is based on a u-net structure and trained on a dataset available on github. Tested on photos of my hand. Changing the existing model was not too hard, however it was time consuming as the dimensions of the photos were different, and the masks had different color dimensions (2 vs 3). 
a)	U-Net Image Segmentation in Keras - PyImageSearch - design structure used in the model 
b)	GitHub - ademakdogan/nails_segmentation: This project was made for nails segmentation using deep learning models. __DeepLabV3Plus__ was used for segmentation problem. ResNet101 were used as encoder and imagenet weights were used as encoder weights. - dataset used to train the model 
The Deep Learning model was created in Visual Studio using python. The model that I was inspired by was in Google collabs and I believe the model that I made can be also adjusted to be used in google collabs. 

![image](https://github.com/ZariaRiv/NailApp/assets/114955463/11a4c762-4995-4d67-922d-2d7f2557cc97)
