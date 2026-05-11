import os
import re

frontend_dir = 'frontend/src'
properties_en = 'src/resources/messages_en.properties'
properties_ru = 'src/resources/messages_ru.properties'
properties_kz = 'src/resources/messages_kz.properties'

def slugify(text):
    text = text.lower()
    text = re.sub(r'[^a-z0-9]+', '_', text)
    text = text.strip('_')
    return text[:40]

def process_files():
    new_keys = {}
    
    existing_keys = set()
    with open(properties_en, 'r', encoding='utf-8') as f:
        for line in f:
            if '=' in line:
                key = line.split('=', 1)[0].strip()
                existing_keys.add(key)
                
    for root, dirs, files in os.walk(frontend_dir):
        for file in files:
            if not file.endswith(('.jsx', '.js')): continue
            filepath = os.path.join(root, file)
            
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
                
            pattern = re.compile(r't\((["\'])(.*?)\1')
            
            def replace_match(match):
                quote = match.group(1)
                text = match.group(2)
                
                if '.' in text and ' ' not in text and text.islower():
                    return match.group(0)
                
                slug = slugify(text)
                if not slug:
                    return match.group(0)
                
                key = f"ui.{slug}"
                
                original_key = key
                counter = 1
                while key in new_keys and new_keys[key] != text:
                    key = f"{original_key}_{counter}"
                    counter += 1
                
                new_keys[key] = text
                return f't({quote}{key}{quote}'
            
            new_content = pattern.sub(replace_match, content)
            
            if new_content != content:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {filepath}")
                
    if new_keys:
        print(f"Adding {len(new_keys)} new keys to properties files.")
        
        with open(properties_en, 'a', encoding='utf-8') as f:
            f.write('\n# Auto-generated UI keys\n')
            for key, val in new_keys.items():
                if key not in existing_keys:
                    f.write(f'{key}={val}\n')
                    
        with open(properties_ru, 'a', encoding='utf-8') as f:
            f.write('\n# Auto-generated UI keys (needs translation)\n')
            for key, val in new_keys.items():
                if key not in existing_keys:
                    f.write(f'{key}={val}\n')
                    
        with open(properties_kz, 'a', encoding='utf-8') as f:
            f.write('\n# Auto-generated UI keys (needs translation)\n')
            for key, val in new_keys.items():
                if key not in existing_keys:
                    f.write(f'{key}={val}\n')

process_files()
