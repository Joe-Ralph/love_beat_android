from PIL import Image

def get_gif_loop_count(file_path):
    with Image.open(file_path) as img:
        if img.is_animated:
            # Extract the loop count from the info dictionary
            loop_count = img.info.get("loop", 1)
            if loop_count == 0:
                return "Infinite loop"
            else:
                return f"Loops {loop_count} times"
        else:
            return "Not an animated GIF"

# Example usage
file_path = "heart.gif"
result = get_gif_loop_count(file_path)
print(result)
