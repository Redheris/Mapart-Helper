### Changes and new features

- Added new option for auxiliary blocks placement to don't place them at all, including the top row;
- Added multithreading to color conversion (doesn't affect error diffusion dithering (Floyd-Steinberg, Atkinson, etc.)). It is opt-in and can be toggled in the config. Better test it on different systems for a while, so it's marked as experimental for now;
- Improved /mart save: image direction from frames on floor and ceiling now depends on player's view direction;