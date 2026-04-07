const sharp = require('sharp');
const path = require('path');

const sizes = [72, 96, 128, 144, 152, 192, 384, 512];
const input = path.join(__dirname, 'public', 'favicon.png');
const outputDir = path.join(__dirname, 'public', 'icons');

async function generate() {
  for (const size of sizes) {
    await sharp(input)
      .resize(size, size, { fit: 'cover' })
      .png()
      .toFile(path.join(outputDir, `icon-${size}x${size}.png`));
    console.log(`Generated icon-${size}x${size}.png`);
  }
  console.log('All icons generated!');
}

generate().catch(console.error);
