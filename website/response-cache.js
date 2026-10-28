// Cache only completed results; conversations always go to JuaGuide.
const VERSION='juafit-ai-cache-v2-holistic2026';
export async function cacheKey(path,payload,userId=''){
 if(path!=='/api/course-facts' && !(path==='/api/analyze'&&userId))return null;
 const bytes=new TextEncoder().encode(JSON.stringify([VERSION,path,userId,payload]));
 return VERSION+'-'+Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',bytes)),b=>b.toString(16).padStart(2,'0')).join('');
}
export function readCache(store,key,now=Date.now()){
 if(!key)return null;
 try{const entry=JSON.parse(store.getItem(key));if(entry&&entry.expires>now)return entry.value;store.removeItem(key);}catch{}
 return null;
}
export function writeCache(store,key,value,ttl,now=Date.now()){
 if(!key)return;
 try{
  const keys=Object.keys(store).filter(k=>k.startsWith(VERSION));
  for(const k of keys){try{if(JSON.parse(store.getItem(k)).expires<=now)store.removeItem(k);}catch{store.removeItem(k);}}
  if(keys.length>=100)store.removeItem(keys[0]);
  store.setItem(key,JSON.stringify({expires:now+ttl,value}));
 }catch{} // Storage limits must never prevent a successful response.
}
