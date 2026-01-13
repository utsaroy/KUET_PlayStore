# Rating and Download Features Implementation

## Overview
The app detail view now fully supports rating and download functionality for the KUET Play Store desktop application.

## Features Implemented

### 1. Download Functionality
- **Download Button**: Located in the app detail view, allows users to download app packages
- **File Selection**: Users can choose where to save the downloaded file via a directory chooser dialog
- **Download Counter**: Automatically increments the download counter in the database
- **Progress Feedback**: Shows "Downloading..." during the download process
- **Error Handling**: Validates user login status and file existence before downloading
- **File Size Display**: Shows formatted file size (KB, MB, GB)

**How it works:**
1. User clicks the "Download" button
2. System checks if user is logged in
3. User selects download location via directory chooser
4. File is copied from `playstore_uploads/packages/` to the selected location
5. Download counter is incremented in the database
6. Success message displays the download location

### 2. Rating Functionality
- **Rating Dialog**: Interactive dialog with 1-5 star rating options using radio buttons
- **Update Support**: Users can update their existing ratings
- **Average Rating Display**: Shows calculated average rating with one decimal place
- **Rating Count**: Displays total number of reviews
- **Rating Distribution**: Visual progress bars showing breakdown of ratings (5-star, 4-star, etc.)
- **User Reviews**: Displays recent user reviews with star ratings and timestamps
- **Login Validation**: Ensures users are logged in before submitting ratings

**How it works:**
1. User clicks "Write a Review" (or "Update Rating" if already rated)
2. Dialog appears with 1-5 star radio button options
3. User's existing rating (if any) is pre-selected
4. User selects new rating and clicks OK
5. Rating is saved/updated in the database
6. Rating statistics are refreshed automatically
7. Button text updates to show current rating

### 3. Dynamic UI Updates
All app detail information is loaded dynamically from the database:
- App title, developer, and category
- App version and size
- Average rating and review count
- Download count (formatted: 1.2K, 5.3M, etc.)
- Rating distribution bars
- Recent user reviews (latest 5)

## Database Schema

### Ratings Table
```sql
CREATE TABLE IF NOT EXISTS ratings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    app_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (app_id) REFERENCES apps (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE (app_id, user_id)
);
```

### Apps Table (relevant fields)
- `downloads` (INTEGER): Tracks number of downloads
- `apk_file_path` (TEXT): Relative path to package file

## Key Methods

### DatabaseHelper.java
- `createOrUpdateRating(appId, userId, rating)`: Create or update a user's rating
- `getRatingsByApp(appId)`: Get all ratings for an app
- `getRatingByAppAndUser(appId, userId)`: Get specific user's rating
- `getAverageRating(appId)`: Calculate average rating
- `getRatingCount(appId)`: Get total number of ratings
- `incrementDownloads(appId)`: Increment download counter

### AppDetailViewController.java
- `handleDownload()`: Process app download
- `handleRateApp()`: Show rating dialog and submit rating
- `loadRatings()`: Load and display rating statistics
- `loadReviews(ratings)`: Display recent user reviews
- `checkUserRating()`: Check if user has already rated

### FileUploadService.java
- `getFullPath(relativePath)`: Convert relative path to absolute file path

## UI Components (FXML)

### Added fx:id attributes:
- `appTitle`, `appDeveloper`, `appCategory`
- `appVersion`, `appSize`, `appDescription`
- `averageRatingText`, `averageRatingTextLarge`
- `ratingCountText`, `ratingCountTextSmall`
- `downloadsText`
- `downloadButton` (with onAction="#handleDownload")
- `rateButton` (with onAction="#handleRateApp")
- `rating1Bar` through `rating5Bar` (progress bars)
- `rating1Count` through `rating5Count` (counts)
- `reviewsContainer` (VBox for dynamic review cards)

## Testing

### Prerequisites
1. Database must have at least one app with `apk_file_path` set
2. User must be logged in to rate or download
3. Package file must exist in `playstore_uploads/packages/` directory

### Test Scenarios

**Download Feature:**
1. Navigate to any app detail page
2. Click "Download" button
3. Select a download location
4. Verify file is copied successfully
5. Verify download count increments

**Rating Feature:**
1. Navigate to any app detail page
2. Click "Write a Review" button
3. Select a star rating (1-5)
4. Click OK
5. Verify rating is saved
6. Verify average rating updates
7. Verify rating distribution updates
8. Click "Update Rating" to change your rating
9. Verify updated rating is saved

## Error Handling

### Download Errors:
- User not logged in: "Please login to download apps"
- Package file not found: "Package file not found" or "Package file does not exist"
- Download cancelled: Silent (no error)
- File copy error: "Download failed: [error message]"

### Rating Errors:
- User not logged in: "Please login to rate apps"
- Rating submission failed: "Failed to submit rating"
- Invalid rating: Prevented by validation (1-5 only)

## File Size Limits
- Package files: Maximum 100 MB
- Icons: Maximum 5 MB
- Screenshots: Maximum 10 MB

## Future Enhancements
- Add review comments/text (currently only star ratings)
- Add "helpful" voting for reviews
- Add file download progress bar with percentage
- Add pause/resume download functionality
- Add rating filter options (e.g., show only 5-star reviews)
- Add sort options for reviews (most recent, most helpful, etc.)
- Add user profile pictures in reviews
