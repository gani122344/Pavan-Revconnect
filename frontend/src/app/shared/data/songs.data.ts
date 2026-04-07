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

  // 🌹 Romantic
  { title: 'Vachinde', artist: 'Sid Sriram', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=VC&background=7b1fa2&color=fff&size=80' },
  { title: 'Butta Bomma', artist: 'Armaan Malik', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=BB&background=9c27b0&color=fff&size=80' },
  { title: 'Srivalli', artist: 'Sid Sriram', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=SV&background=6a1b9a&color=fff&size=80' },
  { title: 'Ramuloo Ramulaa', artist: 'Anurag Kulkarni & Mangli', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=RR&background=8e24aa&color=fff&size=80' },
  { title: 'Choosale Kallaraa', artist: 'Hesham Abdul Wahab', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=CK&background=ab47bc&color=fff&size=80' },
  { title: 'Ninnu Choodagane', artist: 'Anurag Kulkarni', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=NC&background=ce93d8&color=fff&size=80' },
  { title: 'Ye Maaya Chesave Title Song', artist: 'A.R. Rahman', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=YM&background=4a148c&color=fff&size=80' },
  { title: 'Kanulu Navainaa', artist: 'Anurag Kulkarni', genre: 'Romantic', coverUrl: 'https://ui-avatars.com/api/?name=KN&background=ba68c8&color=fff&size=80' },

  // 💪 Motivation
  { title: 'Jai Balayya', artist: 'S. Thaman', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=JB&background=ff6d00&color=fff&size=80' },
  { title: 'Dosti', artist: 'Yuvan Shankar Raja', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=DS&background=e65100&color=fff&size=80' },
  { title: 'Jolly O Gymkhana', artist: 'DSP', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=JG&background=bf360c&color=fff&size=80' },
  { title: 'Dham Dham', artist: 'Anurag Kulkarni', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=DD&background=d84315&color=fff&size=80' },
  { title: 'Teenmaar', artist: 'DSP', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=TM&background=ff3d00&color=fff&size=80' },
  { title: 'Lux Papa Lux', artist: 'S. Thaman', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=LP&background=ff9100&color=fff&size=80' },
  { title: 'Mind Block', artist: 'Blaaze', genre: 'Motivation', coverUrl: 'https://ui-avatars.com/api/?name=MB&background=ff6e40&color=fff&size=80' },

  // 😢 Sad
  { title: 'Kanunna Kalyanam', artist: 'Sri Krishna', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=KK&background=455a64&color=fff&size=80' },
  { title: 'Nee Valle', artist: 'Sid Sriram', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=NV&background=546e7a&color=fff&size=80' },
  { title: 'Manasu Maree', artist: 'Chinmayi', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=MM&background=37474f&color=fff&size=80' },
  { title: 'Ye Kadha Nee Kadha', artist: 'A.R. Rahman', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=YK&background=607d8b&color=fff&size=80' },
  { title: 'Pranamam', artist: 'Sid Sriram', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=PM&background=78909c&color=fff&size=80' },
  { title: 'Naa Kanulu Yepudu', artist: 'Sid Sriram', genre: 'Sad', coverUrl: 'https://ui-avatars.com/api/?name=NY&background=263238&color=fff&size=80' },

  // 🙏 Devotional
  { title: 'Koluvaithiva Rangasayi', artist: 'S.P. Balasubrahmanyam', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=KR&background=ff8f00&color=fff&size=80' },
  { title: 'Sittharala Sirapadu', artist: 'Anurag Kulkarni', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=SS&background=f9a825&color=fff&size=80' },
  { title: 'Om Namah Shivaya', artist: 'S.P. Balasubrahmanyam', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=ON&background=fbc02d&color=000&size=80' },
  { title: 'Sree Ragam', artist: 'K.J. Yesudas', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=SR&background=fdd835&color=000&size=80' },
  { title: 'Govinda Govinda', artist: 'S.P. Balasubrahmanyam', genre: 'Devotional', coverUrl: 'https://ui-avatars.com/api/?name=GG&background=ffb300&color=fff&size=80' },

  // 🎉 Party
  { title: 'Arabic Kuthu', artist: 'Anirudh Ravichander', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=AK&background=00c853&color=fff&size=80' },
  { title: 'Naatu Naatu', artist: 'Rahul Sipligunj & Kaala Bhairava', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=NN&background=00e676&color=000&size=80' },
  { title: 'Top Lesi Poddi', artist: 'Benny Dayal & Ranina Reddy', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=TL&background=1b5e20&color=fff&size=80' },
  { title: 'Aa Ante Amalapuram', artist: 'M.M. Keeravani', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=AA&background=2e7d32&color=fff&size=80' },
  { title: 'Ranga Ranga Rangasthalaana', artist: 'DSP', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=RR&background=388e3c&color=fff&size=80' },
  { title: 'Swing Zara', artist: 'DSP', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=SZ&background=43a047&color=fff&size=80' },
  { title: 'Pakka Local', artist: 'DSP', genre: 'Party', coverUrl: 'https://ui-avatars.com/api/?name=PL&background=66bb6a&color=fff&size=80' },

  // 🔥 Mass
  { title: 'Akhanda Title Song', artist: 'S. Thaman', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=AK&background=b71c1c&color=fff&size=80' },
  { title: 'Simha Raasi', artist: 'Anurag Kulkarni', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=SR&background=c62828&color=fff&size=80' },
  { title: 'Saahore Baahubali', artist: 'M.M. Keeravani', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=SB&background=d32f2f&color=fff&size=80' },
  { title: 'Jai Lava Kusa Title Song', artist: 'DSP', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=JL&background=e53935&color=fff&size=80' },
  { title: 'Dandalayya', artist: 'M.M. Keeravani', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=DL&background=ef5350&color=fff&size=80' },
  { title: 'Veera Simha Reddy', artist: 'S. Thaman', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=VS&background=f44336&color=fff&size=80' },
  { title: 'Saami Saami', artist: 'Mounika Yadav', genre: 'Mass', coverUrl: 'https://ui-avatars.com/api/?name=SM&background=ff1744&color=fff&size=80' },
];

export const SONG_GENRES = [...new Set(SONG_LIBRARY.map(s => s.genre))];
