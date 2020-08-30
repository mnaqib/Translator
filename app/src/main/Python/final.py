
from googletrans import Translator


def input(a,b):
    get_sentence = a

    # Translator method for translation

    translator = Translator()

    # short form of english in which
    # you will speak

    from_lang = 'en'

    # Using translate() method which requires
    # three arguments, 1st the sentence which
    # needs to be translated 2nd source language
    # and 3rd to which we need to translate in

    text_to_translate = translator.translate(get_sentence, src= from_lang, dest= b)

    # Storing the translated text in text
    # variable

    text = text_to_translate.text

    return text

def lang_key(c):
    country={
        'HINDI': 'hi',
        'BENGALI': 'bn',
        'KANNADA' : 'kn',
        'MARATHI' : 'mr',
        'PUNJABI' : 'pa',
        'TAMIL' : 'ta',
        'TELUGU' : 'te',
        'MALAYALAM' : 'ml',
        'GUJARATI' : 'gu',
        "ODIA" : 'or'
    }
    x = country.get(c)
    return x

def lang_script(c):
    country={
        'HINDI': 'हिन्दी',
        'BENGALI': 'বাংলা',
        'KANNADA' : 'ಕನ್ನಡ',
        'MARATHI' : 'मराठी',
        'PUNJABI' : 'ਪੰਜਾਬੀ',
        'TAMIL' : 'தமிழ்',
        'TELUGU' : 'తెలుగు',
        'MALAYALAM' : 'മലയാളം',
        'GUJARATI' : 'ગુજરાતી',
        'ODIA' :'ଓଡ଼ିଆ'
    }
    x = country.get(c)
    return x

