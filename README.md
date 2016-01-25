# Trevie - Your Movie Buddy

Trevie is an Android app that allows users to discover the most popular movies playing. The project was created as part of Udacity Android Nanodegree.

## Main Features

- Displays grid of movies fetched from [TheMovieDB](https://www.themoviedb.org/)
- Allows users to sort movies by highest rating or popularity
- Shows additional movie information upon tap on a movie poster
- Plays movie trailers in external app
- Lets users share trailers via share intent
- Stores favourite movies in local database
- Features master-detail tablet layout

## Requirements and Permissions
The app runs on Android Ice Cream Sandwich (4.0.1 / API 14) and onwards.

Required permissions:
- **INTERNET**: To connect with [TheMovieDB](https://www.themoviedb.org/).
- **ACCESS_NETWORK_STATE**: To check if internet connection is available.

## Try it out
Try Trevie by following these steps:

1. Download repo
2. Add [TheMovieDB](https://www.themoviedb.org/) API key to the build
3. Compile & Enjoy!

### TheMovieDB API Key is required
In order for the Trevie app to function properly an API key for [TheMovieDB](https://www.themoviedb.org/) must be included with the build. Please include your own key for the build by adding the following line to [USER_HOME]/.gradle/gradle.properties

`MyTheMovieDBApiKey="<UNIQUE_API_KEY">`

## License
Published under [The MIT License](https://opensource.org/licenses/MIT).
