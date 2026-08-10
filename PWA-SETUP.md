# Fuel PWA files

Upload these files to the root of the `fuel-tracker` GitHub repository:

- `index.html` — your existing app, updated with PWA metadata and service-worker registration
- `manifest.webmanifest` — tells Android/Chrome how to install and launch Fuel
- `sw.js` — service worker for installability/offline app-shell support
- `icon-192.png`
- `icon-512.png`
- `icon-maskable-512.png`

After GitHub Pages deploys the changes, open the site in Chrome on Android and use:
**⋮ → Add to Home screen / Install app**

Launch Fuel from the newly installed home-screen/app-drawer icon. It should open in standalone mode without Chrome's normal address/tab bar.

If you previously added Fuel only as a normal bookmark/shortcut, remove that old shortcut and install it again after these files are live.
