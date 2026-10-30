import {consentReceipt} from './consent.js';
// Frontend preview only. This does not protect server data or establish trusted identity.
const key='juafit-preview-accounts';
const sessionKey='juafit-preview-session';
const listeners=new Set();
const bytesToHex=bytes=>Array.from(bytes,b=>b.toString(16).padStart(2,'0')).join('');
async function digest(password,salt){
 const material=await crypto.subtle.importKey('raw',new TextEncoder().encode(password),'PBKDF2',false,['deriveBits']);
 return bytesToHex(new Uint8Array(await crypto.subtle.deriveBits({name:'PBKDF2',salt:new TextEncoder().encode(salt),iterations:120000,hash:'SHA-256'},material,256)));
}
const publicUser=account=>({uid:account.uid,displayName:account.displayName,email:account.email,preview:true});
const error=code=>Object.assign(new Error(code),{code});
function accounts(store){try{return JSON.parse(store.getItem(key)||'[]')}catch{return []}}
function emit(user){listeners.forEach(callback=>callback(user));}
export function previewUser(store=localStorage,session=sessionStorage){
 try{const uid=session.getItem(sessionKey);const account=accounts(store).find(a=>a.uid===uid);return account?publicUser(account):null;}catch{return null;}
}
export async function previewRegister(values,store=localStorage,session=sessionStorage){
 const consent=consentReceipt(values.consentAccepted);
 const existing=accounts(store),email=values.email.trim().toLowerCase();
 if(existing.some(a=>a.email===email))throw error('auth/email-already-in-use');
 const salt=bytesToHex(crypto.getRandomValues(new Uint8Array(16)));
 const account={consent,uid:crypto.randomUUID(),displayName:values.name.trim(),email,salt,verifier:await digest(values.password,salt)};
 existing.push(account);
 try{store.setItem(key,JSON.stringify(existing));session.setItem(sessionKey,account.uid);}catch{throw error('auth/preview-storage-unavailable');}
 const user=publicUser(account);emit(user);return {user,profileUpdated:true};
}
export async function previewLogin(email,password,store=localStorage,session=sessionStorage){
 const account=accounts(store).find(a=>a.email===email.trim().toLowerCase());
 if(!account||await digest(password,account.salt)!==account.verifier)throw error('auth/invalid-credential');
 try{session.setItem(sessionKey,account.uid);}catch{throw error('auth/preview-storage-unavailable');}
 const user=publicUser(account);emit(user);return {user};
}
export function previewLogout(session=sessionStorage){session.removeItem(sessionKey);emit(null);}
export function observePreview(callback){listeners.add(callback);callback(previewUser());return ()=>listeners.delete(callback);}
