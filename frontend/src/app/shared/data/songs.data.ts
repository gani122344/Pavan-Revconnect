export interface Song {
  title: string;
  artist: string;
  genre: string;
  coverUrl: string;
}

export const SONG_LIBRARY: Song[] = [
  // ❤️ Love
  { title: 'Inkem Inkem Inkem Kaavaale', artist: 'Sid Sriram', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=IK&background=e91e63&color=fff&size=80' },
  { title: 'Samajavaragamana', artist: 'Sid Sriram', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=SJ&background=c2185b&color=fff&size=80' },
  { title: 'Kushi Title Song', artist: 'Hesham Abdul Wahab', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=KS&background=d81b60&color=fff&size=80' },
  { title: 'Ye Nimishamulo', artist: 'DSP', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=YN&background=f06292&color=fff&size=80' },
  { title: 'Pillaa Raa', artist: 'Sid Sriram', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=PR&background=ec407a&color=fff&size=80' },
  { title: 'Nee Kannu Neeli Samudram', artist: 'Sid Sriram', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=NK&background=ad1457&color=fff&size=80' },
  { title: 'Oo Antava', artist: 'Indravathi Chauhan', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=OA&background=880e4f&color=fff&size=80' },
  { title: 'Nuvvu Naaku Nachav', artist: 'S.P. Balasubrahmanyam', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=NN&background=ff80ab&color=fff&size=80' },
  { title: 'Tharagathi Gadhi', artist: 'Sid Sriram', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=TG&background=f48fb1&color=fff&size=80' },
  { title: 'Ee Raathale', artist: 'Sid Sriram', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=ER&background=f06292&color=fff&size=80' },
  { title: 'Nee Neeli Kannullona', artist: 'Vijay Devarakonda', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=NK&background=ec407a&color=fff&size=80' },
  { title: 'Manasuku Nachindi', artist: 'Chinmayi', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=MN&background=e91e63&color=fff&size=80' },
  { title: 'Undiporaadhey', artist: 'Sid Sriram', genre: 'Love', coverUrl: 'https://ui-avatars.com/api/?name=UP&background=c2185b&color=fff&size=80' },

  // 🌹 Romantic
  { title: 'Vachinde', artist: 'Sid Sriram', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=VC&background=7b1fa2&color=fff&size=80' },
  { title: 'Butta Bomma', artist: 'Armaan Malik', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=BB&background=9c27b0&color=fff&size=80' },
  { title: 'Srivalli', artist: 'Sid Sriram', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=SV&background=6a1b9a&color=fff&size=80' },
  { title: 'Ramuloo Ramulaa', artist: 'Anurag Kulkarni & Mangli', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=RR&background=8e24aa&color=fff&size=80' },
  { title: 'Choosale Kallaraa', artist: 'Hesham Abdul Wahab', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=CK&background=ab47bc&color=fff&size=80' },
  { title: 'Ninnu Choodagane', artist: 'Anurag Kulkarni', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=NC&background=ce93d8&color=fff&size=80' },
  { title: 'Ye Maaya Chesave Title Song', artist: 'A.R. Rahman', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=YM&background=4a148c&color=fff&size=80' },
  { title: 'Kanulu Navainaa', artist: 'Anurag Kulkarni', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=KN&background=ba68c8&color=fff&size=80' },
  { title: 'Saahasam Swaasaga Saagipo', artist: 'A.R. Rahman', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=SS&background=9575cd&color=fff&size=80' },
  { title: 'Oka Laila Kosam', artist: 'Anup Rubens', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=OL&background=7e57c2&color=fff&size=80' },
  { title: 'Nuvvunte Naa Jathaga', artist: 'Anand Aravindakshan', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=NJ&background=673ab7&color=fff&size=80' },
  { title: 'Manasa Manasa', artist: 'Sid Sriram', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=MM&background=8e24aa&color=fff&size=80' },
  { title: 'Merise Merise', artist: 'Sid Sriram', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=MR&background=ab47bc&color=fff&size=80' },

  // 💪 Motivation
  { title: 'Jai Balayya', artist: 'S. Thaman', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=JB&background=ff6d00&color=fff&size=80' },
  { title: 'Dosti', artist: 'Yuvan Shankar Raja', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=DS&background=e65100&color=fff&size=80' },
  { title: 'Jolly O Gymkhana', artist: 'DSP', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=JG&background=bf360c&color=fff&size=80' },
  { title: 'Dham Dham', artist: 'Anurag Kulkarni', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=DD&background=d84315&color=fff&size=80' },
  { title: 'Teenmaar', artist: 'DSP', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=TM&background=ff3d00&color=fff&size=80' },
  { title: 'Lux Papa Lux', artist: 'S. Thaman', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=LP&background=ff9100&color=fff&size=80' },
  { title: 'Mind Block', artist: 'Blaaze', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=MB&background=ff6e40&color=fff&size=80' },
  { title: 'Vakeel Saab Title Song', artist: 'Sid Sriram', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=VS&background=e65100&color=fff&size=80' },
  { title: 'Khaleja Title Song', artist: 'Shankar Mahadevan', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=KH&background=ff6d00&color=fff&size=80' },
  { title: 'Gudilo Badilo', artist: 'Benny Dayal', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=GB&background=bf360c&color=fff&size=80' },

  // 😢 Sad
  { title: 'Kanunna Kalyanam', artist: 'Sri Krishna', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=KK&background=455a64&color=fff&size=80' },
  { title: 'Nee Valle', artist: 'Sid Sriram', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=NV&background=546e7a&color=fff&size=80' },
  { title: 'Manasu Maree', artist: 'Chinmayi', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=MM&background=37474f&color=fff&size=80' },
  { title: 'Ye Kadha Nee Kadha', artist: 'A.R. Rahman', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=YK&background=607d8b&color=fff&size=80' },
  { title: 'Pranamam', artist: 'Sid Sriram', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=PM&background=78909c&color=fff&size=80' },
  { title: 'Naa Kanulu Yepudu', artist: 'Sid Sriram', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=NY&background=263238&color=fff&size=80' },
  { title: 'Emai Poyave', artist: 'Sid Sriram', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=EP&background=546e7a&color=fff&size=80' },
  { title: 'Nee Jathaga', artist: 'Karthik', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=NJ&background=455a64&color=fff&size=80' },
  { title: 'Maate Vinadhuga', artist: 'Sid Sriram', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=MV&background=37474f&color=fff&size=80' },
  { title: 'Nenu Lenu', artist: 'Haricharan', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=NL&background=607d8b&color=fff&size=80' },

  // 🙏 Devotional
  { title: 'Koluvaithiva Rangasayi', artist: 'S.P. Balasubrahmanyam', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=KR&background=ff8f00&color=fff&size=80' },
  { title: 'Sittharala Sirapadu', artist: 'Anurag Kulkarni', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=SS&background=f9a825&color=fff&size=80' },
  { title: 'Om Namah Shivaya', artist: 'S.P. Balasubrahmanyam', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=ON&background=fbc02d&color=000&size=80' },
  { title: 'Sree Ragam', artist: 'K.J. Yesudas', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=SR&background=fdd835&color=000&size=80' },
  { title: 'Govinda Govinda', artist: 'S.P. Balasubrahmanyam', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=GG&background=ffb300&color=fff&size=80' },
  { title: 'Annamayya Title Song', artist: 'S.P. Balasubrahmanyam', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=AN&background=ffa000&color=fff&size=80' },
  { title: 'Bhaje Bhaaje', artist: 'Anurag Kulkarni', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=BB&background=ff8f00&color=fff&size=80' },
  { title: 'Swagatham Krishna', artist: 'Sid Sriram', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=SK&background=f9a825&color=fff&size=80' },

  // 🎉 Party
  { title: 'Arabic Kuthu', artist: 'Anirudh Ravichander', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=AK&background=00c853&color=fff&size=80' },
  { title: 'Naatu Naatu', artist: 'Rahul Sipligunj & Kaala Bhairava', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=NN&background=00e676&color=000&size=80' },
  { title: 'Top Lesi Poddi', artist: 'Benny Dayal & Ranina Reddy', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=TL&background=1b5e20&color=fff&size=80' },
  { title: 'Aa Ante Amalapuram', artist: 'M.M. Keeravani', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=AA&background=2e7d32&color=fff&size=80' },
  { title: 'Ranga Ranga Rangasthalaana', artist: 'DSP', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=RR&background=388e3c&color=fff&size=80' },
  { title: 'Swing Zara', artist: 'DSP', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=SZ&background=43a047&color=fff&size=80' },
  { title: 'Pakka Local', artist: 'DSP', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=PL&background=66bb6a&color=fff&size=80' },
  { title: 'Jigelu Rani', artist: 'Rahul Sipligunj & Kaala Bhairava', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=JR&background=4caf50&color=fff&size=80' },
  { title: 'Daang Daang', artist: 'Anurag Kulkarni', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=DD&background=2e7d32&color=fff&size=80' },
  { title: 'Tattad Tattad', artist: 'Arijit Singh', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=TT&background=1b5e20&color=fff&size=80' },
  { title: 'Rowdy Baby', artist: 'Dhanush & Dhee', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=RB&background=00c853&color=fff&size=80' },
  { title: 'Kamariya', artist: 'Arijit Singh', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=KM&background=388e3c&color=fff&size=80' },

  // 🔥 Mass
  { title: 'Akhanda Title Song', artist: 'S. Thaman', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=AK&background=b71c1c&color=fff&size=80' },
  { title: 'Simha Raasi', artist: 'Anurag Kulkarni', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=SR&background=c62828&color=fff&size=80' },
  { title: 'Saahore Baahubali', artist: 'M.M. Keeravani', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=SB&background=d32f2f&color=fff&size=80' },
  { title: 'Jai Lava Kusa Title Song', artist: 'DSP', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=JL&background=e53935&color=fff&size=80' },
  { title: 'Dandalayya', artist: 'M.M. Keeravani', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=DL&background=ef5350&color=fff&size=80' },
  { title: 'Veera Simha Reddy', artist: 'S. Thaman', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=VS&background=f44336&color=fff&size=80' },
  { title: 'Saami Saami', artist: 'Mounika Yadav', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=SM&background=ff1744&color=fff&size=80' },
  { title: 'Deva Deva', artist: 'Arijit Singh', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=DD&background=b71c1c&color=fff&size=80' },
  { title: 'Pushpa Pushpa', artist: 'DSP', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=PP&background=c62828&color=fff&size=80' },
  { title: 'Eyy Bidda Idhi Naa Adda', artist: 'Rahul Sipligunj', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=EB&background=d32f2f&color=fff&size=80' },
  { title: 'Bujji Theme', artist: 'DSP', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=BT&background=e53935&color=fff&size=80' },
  { title: 'Poolamme Pilla', artist: 'Sid Sriram', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=PP&background=ef5350&color=fff&size=80' },

  // 🎧 Trending
  { title: 'Kesariya', artist: 'Arijit Singh', genre: 'Trending', coverUrl: 'https://ui-avatars.com/api/?name=KE&background=1565c0&color=fff&size=80' },
  { title: 'Tum Hi Ho', artist: 'Arijit Singh', genre: 'Trending', coverUrl: 'https://ui-avatars.com/api/?name=TH&background=1976d2&color=fff&size=80' },
  { title: 'Chaleya', artist: 'Arijit Singh', genre: 'Trending', coverUrl: 'https://ui-avatars.com/api/?name=CH&background=1e88e5&color=fff&size=80' },
  { title: 'Tera Ban Jaunga', artist: 'Akhil Sachdeva', genre: 'Trending', coverUrl: 'https://ui-avatars.com/api/?name=TB&background=2196f3&color=fff&size=80' },
  { title: 'Apna Bana Le', artist: 'Arijit Singh', genre: 'Trending', coverUrl: 'https://ui-avatars.com/api/?name=AB&background=42a5f5&color=fff&size=80' },
  { title: 'Tere Vaaste', artist: 'Varun Jain', genre: 'Trending', coverUrl: 'https://ui-avatars.com/api/?name=TV&background=1565c0&color=fff&size=80' },
  { title: 'Love Nwantiti', artist: 'CKay', genre: 'Trending', coverUrl: 'https://ui-avatars.com/api/?name=LN&background=1976d2&color=fff&size=80' },
  { title: 'Pasoori', artist: 'Ali Sethi & Shae Gill', genre: 'Trending', coverUrl: 'https://ui-avatars.com/api/?name=PA&background=1e88e5&color=fff&size=80' },

  // 🎤 Hip-Hop
  { title: 'Hukum', artist: 'Anirudh Ravichander', genre: 'Hip-Hop', coverUrl: 'https://ui-avatars.com/api/?name=HK&background=311b92&color=fff&size=80' },
  { title: 'King Of Kotha', artist: 'Jakes Bejoy', genre: 'Hip-Hop', coverUrl: 'https://ui-avatars.com/api/?name=KK&background=4527a0&color=fff&size=80' },
  { title: 'Naa Ready', artist: 'Anirudh Ravichander', genre: 'Hip-Hop', coverUrl: 'https://ui-avatars.com/api/?name=NR&background=512da8&color=fff&size=80' },
  { title: 'Illuminati', artist: 'Sushin Shyam', genre: 'Hip-Hop', coverUrl: 'https://ui-avatars.com/api/?name=IL&background=5e35b1&color=fff&size=80' },
  { title: 'Sher Aaya Sher', artist: 'Divine', genre: 'Hip-Hop', coverUrl: 'https://ui-avatars.com/api/?name=SS&background=311b92&color=fff&size=80' },
  { title: 'Kar Har Maidaan Fateh', artist: 'Sukhwinder Singh', genre: 'Hip-Hop', coverUrl: 'https://ui-avatars.com/api/?name=KH&background=4527a0&color=fff&size=80' },

  // 🎵 Chill
  { title: 'Aaoge Jab Tum', artist: 'Ustad Rashid Khan', genre: 'Chill', coverUrl: 'https://ui-avatars.com/api/?name=AJ&background=00695c&color=fff&size=80' },
  { title: 'Iktara', artist: 'Amit Trivedi', genre: 'Chill', coverUrl: 'https://ui-avatars.com/api/?name=IK&background=00796b&color=fff&size=80' },
  { title: 'Tujhe Kitna Chahne Lage', artist: 'Arijit Singh', genre: 'Chill', coverUrl: 'https://ui-avatars.com/api/?name=TK&background=00897b&color=fff&size=80' },
  { title: 'Phir Le Aaya Dil', artist: 'Arijit Singh', genre: 'Chill', coverUrl: 'https://ui-avatars.com/api/?name=PL&background=009688&color=fff&size=80' },
  { title: 'Tum Se Hi', artist: 'Mohit Chauhan', genre: 'Chill', coverUrl: 'https://ui-avatars.com/api/?name=TS&background=26a69a&color=fff&size=80' },
  { title: 'Agar Tum Saath Ho', artist: 'A.R. Rahman', genre: 'Chill', coverUrl: 'https://ui-avatars.com/api/?name=AT&background=00695c&color=fff&size=80' },
];

export const SONG_GENRES = [...new Set(SONG_LIBRARY.map(s => s.genre))];
