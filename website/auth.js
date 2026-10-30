import {hasConsent,consentReceipt} from './consent.js';
import config from './config.js';
import {previewRegister,previewLogin,previewLogout,observePreview} from './preview-auth.js';

export function hasFirebaseConfig(value=config.firebase){return Boolean(value?.apiKey&&value?.authDomain&&value?.projectId&&value?.appId);}
export function registrationError({name,email,password,confirmPassword,consentAccepted}){
 if(!name?.trim())return 'Please enter your full name.';
 if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email?.trim()||''))return 'Please enter a valid email address.';
 if(!password||password.length<8)return 'Use at least 8 characters for your password.';
 if(password!==confirmPassword)return 'Your passwords do not match.';
 if(!hasConsent(consentAccepted))return 'Please read and agree to the data use and consent notice.';
 return '';
}
export function authError(error){
 const messages={
  'auth/invalid-credential':'The email or password is incorrect. Please try again.',
  'auth/wrong-password':'The email or password is incorrect. Please try again.',
  'auth/user-not-found':'The email or password is incorrect. Please try again.',
  'auth/invalid-email':'Please enter a valid email address.',
  'auth/email-already-in-use':'An account already uses this email. Try logging in or resetting your password.',
  'auth/weak-password':'Choose a stronger password with at least 8 characters.',
  'auth/too-many-requests':'Too many attempts. Please wait a moment and try again.',
  'auth/network-request-failed':'Check your internet connection and try again.',
  'auth/operation-not-allowed':'Account sign-in is being connected. Please try again later.',
  'auth/unauthorized-domain':'Sign-in is not available on this website address yet.',
  'auth/setup-required':'Account sign-in is being connected. You can explore pathways in the meantime.',
  'auth/preview-storage-unavailable':'Allow browser storage to use a preview account on this device.',
  'auth/preview-reset-unavailable':'Password reset emails will be available when Firebase is connected.',
 };
 return messages[error?.code]||'We could not complete sign-in. Please try again.';
}
let connection;
async function firebase(){
 if(!hasFirebaseConfig())throw Object.assign(new Error('Firebase setup required'),{code:'auth/setup-required'});
 if(!connection)connection=Promise.all([
  import('https://www.gstatic.com/firebasejs/13.0.0/firebase-app.js'),
  import('https://www.gstatic.com/firebasejs/13.0.0/firebase-auth.js'),
 ]).then(([app,sdk])=>({sdk,auth:sdk.getAuth(app.getApps().length?app.getApp():app.initializeApp(config.firebase))})).catch(error=>{connection=null;throw error;});
 return connection;
}
export async function registerAccount(values){
 const receipt=consentReceipt(values.consentAccepted);
 const message=registrationError(values);if(message)throw Object.assign(new Error(message),{validation:message});
 if(!hasFirebaseConfig())return previewRegister(values);
 const {auth,sdk}=await firebase();
 const result=await sdk.createUserWithEmailAndPassword(auth,values.email.trim(),values.password);
 // A failed display-name update must not imply that account creation failed.
 let profileUpdated=true;
 try{await sdk.updateProfile(result.user,{displayName:values.name.trim()});}catch{profileUpdated=false;}
 try{localStorage.setItem('juafit-consent-'+result.user.uid,JSON.stringify(receipt));}catch{}
 return {user:result.user,profileUpdated};
}
export async function loginAccount(email,password){if(!hasFirebaseConfig())return previewLogin(email,password);const {auth,sdk}=await firebase();return sdk.signInWithEmailAndPassword(auth,email.trim(),password);}
export async function resetPassword(email){if(!hasFirebaseConfig())throw Object.assign(new Error('Preview only'),{code:'auth/preview-reset-unavailable'});const {auth,sdk}=await firebase();return sdk.sendPasswordResetEmail(auth,email.trim());}
export async function logoutAccount(){if(!hasFirebaseConfig())return previewLogout();const {auth,sdk}=await firebase();return sdk.signOut(auth);}
export async function observeAccount(callback){if(!hasFirebaseConfig())return observePreview(callback);const {auth,sdk}=await firebase();return sdk.onAuthStateChanged(auth,callback);}
