'use client';
import { useState } from "react";

export default function Home() {
  const [image, setImage] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [label, setLabel] = useState<string | null>(null);
  const [conf, setConf] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!image) {
      setError('Please select an image first.');
      return;
    }

    setError(null);
    setLabel(null);
    setConf(null);

    const formData = new FormData();
    formData.append('image', image);

    try {
      const res = await fetch('/api/detect', {
        method: 'POST',
        body: formData,
      });

      const data = await res.json();

      console.log(data)
      if (!res.ok) {
        throw new Error(data.error || 'Failed to detect image.');
      }

      setLabel(data.label || 'Unknown');
      setConf(data.conf ?? null);
      setPreviewUrl(URL.createObjectURL(image));
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Unexpected error occurred.');
      }
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 to-blue-200 flex flex-col items-center justify-between px-4 py-6">
      <header className="text-2xl font-bold text-blue-800 mb-4 text-center">
        YOLOv11 Image Detection Demo
      </header>

      <main className="w-full max-w-xl bg-white p-6 rounded-2xl shadow-xl">
        {error && <p className="text-red-600 font-semibold mb-4">{error}</p>}

        <form onSubmit={handleSubmit} encType="multipart/form-data" className="space-y-4">
          <input
            type="file"
            accept="image/*"
            required
            onChange={(e) => {
              const file = e.target.files?.[0] || null;
              setImage(file);
              setPreviewUrl(file ? URL.createObjectURL(file) : null);
            }}
            className="w-full px-4 py-2 border rounded-lg cursor-pointer"
            style={{ color: 'gray' }}
          />

          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition"
            style={{ cursor: 'pointer' }}
          >
            Detect
          </button>
        </form>

        {previewUrl && (
          <div className="mt-8 text-center">
            <img
              src={previewUrl}
              alt="Uploaded"
              className="w-full max-w-md rounded-xl shadow-md mx-auto mb-4"
            />
            {label && (
              <div className="text-gray-800">
                <p className="font-bold text-lg">Label: <span className="text-blue-600">{label}</span></p>
                <p className="font-semibold">Confidence: {(conf ?? 0 * 100).toFixed(2)}%</p>
              </div>
            )}
          </div>
        )}
      </main>

      <footer className="text-sm text-center text-gray-600 mt-10">
        <p>&copy; KvB - 20225261. All rights reserved.</p>
        <p>
          Contact: <a href="mailto:kieubao2k4@gmail.com" className="text-blue-600">kieubao2k4@gmail.com</a>
        </p>
      </footer>
    </div>
  );
}
