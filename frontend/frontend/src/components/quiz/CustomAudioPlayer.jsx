import { useState, useRef, useEffect } from 'react';
import { Play, Pause, Volume2, VolumeX } from 'lucide-react';

const CustomAudioPlayer = ({ src, title = "Audio cho phần thi nghe" }) => {
  const audioRef = useRef(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [duration, setDuration] = useState(0);
  const [progress, setProgress] = useState(0);
  const [volume, setVolume] = useState(1);

  const formatTime = (timeInSeconds) => {
    if (isNaN(timeInSeconds) || timeInSeconds === 0) return '00:00';
    const minutes = Math.floor(timeInSeconds / 60);
    const seconds = Math.floor(timeInSeconds % 60);
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  };

  // Handlers for audio events
  const handleLoadedMetadata = () => {
    if (audioRef.current) setDuration(audioRef.current.duration);
  };

  const handleTimeUpdate = () => {
    if (audioRef.current) setProgress(audioRef.current.currentTime);
  };

  const handleEnded = () => {
    setIsPlaying(false);
  };

  // Handlers for user interaction
  const togglePlayPause = () => {
    if (isPlaying) {
      audioRef.current.pause();
    } else {
      audioRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  const handleProgressChange = (e) => {
    const newTime = e.target.value;
    if (audioRef.current) {
      audioRef.current.currentTime = newTime;
      setProgress(newTime);
    }
  };

  const handleVolumeChange = (e) => {
    const newVolume = e.target.value;
    if (audioRef.current) {
      audioRef.current.volume = newVolume;
      setVolume(newVolume);
    }
  };

  const toggleMute = () => {
    const newVolume = volume > 0 ? 0 : 1;
    if (audioRef.current) {
      audioRef.current.volume = newVolume;
      setVolume(newVolume);
    }
  };

  return (
    <div className="mb-4 p-4 bg-gray-100 border border-gray-200 rounded-lg">
      <p className="font-medium text-gray-700 mb-3">{title}</p>
      <div className="flex items-center gap-4">
        <audio ref={audioRef} src={src} onLoadedMetadata={handleLoadedMetadata} onTimeUpdate={handleTimeUpdate} onEnded={handleEnded} />
        <button onClick={togglePlayPause} className="p-2 bg-green-600 text-white rounded-full hover:bg-green-700 transition-transform transform hover:scale-110 focus:outline-none">
          {isPlaying ? <Pause size={20} /> : <Play size={20} />}
        </button>
        <span className="text-sm font-mono text-gray-600 w-12 text-center">{formatTime(progress)}</span>
        <div className="flex-grow">
          <input type="range" min="0" max={duration || 0} value={progress} onChange={handleProgressChange} className="w-full h-2 bg-gray-300 rounded-lg appearance-none cursor-pointer accent-green-600" />
        </div>
        <span className="text-sm font-mono text-gray-600 w-12 text-center">{formatTime(duration)}</span>
        <div className="flex items-center gap-2 w-32">
          <button onClick={toggleMute} className="text-gray-600 hover:text-gray-800 focus:outline-none">
            {volume > 0 ? <Volume2 size={20} /> : <VolumeX size={20} />}
          </button>
          <input type="range" min="0" max="1" step="0.05" value={volume} onChange={handleVolumeChange} className="w-full h-2 bg-gray-300 rounded-lg appearance-none cursor-pointer accent-green-600" />
        </div>
      </div>
    </div>
  );
};

export default CustomAudioPlayer;