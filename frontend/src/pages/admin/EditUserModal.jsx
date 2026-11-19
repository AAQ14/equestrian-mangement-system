import { useState } from 'react';
import Button from '../../components/Button';
import { Modal } from '../../components/ui';
import { useToast } from '../../context/ToastContext';
import { usersApi } from '../../api/endpoints';
import { getErrorMessage } from '../../api/axiosInstance';

// Roles an admin can set here. Instructors are added or removed on the Instructors page.
const ROLES = ['CLIENT', 'EVENT_MANAGER', 'ADMIN'];

// Admin form to edit a user's name, email, phone, role and status.
export default function EditUserModal({ user, isMe, onClose, onSaved }) {
    const { showToast } = useToast();
    const [form, setForm] = useState({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone || '',
        role: user.role,
        userStatus: user.userStatus,
    });
    const [saving, setSaving] = useState(false);
    const isInstructor = user.role === 'INSTRUCTOR';

    function handleChange(event) {
        setForm({ ...form, [event.target.name]: event.target.value });
    }

    async function handleSave() {
        if (form.phone && !/^\+?[0-9]{8,15}$/.test(form.phone)) {
            showToast('Phone must have 8 to 15 digits, optionally starting with +.', 'error');
            return;
        }
        // Do not send role or status for yourself or for instructors (the backend rejects them).
        const fields = { ...form };
        if (isMe || isInstructor) delete fields.role;
        if (isMe) delete fields.userStatus;

        setSaving(true);
        try {
            await usersApi.update(user.id, fields);
            showToast(`${form.firstName} was updated.`, 'success');
            onSaved();
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        } finally {
            setSaving(false);
        }
    }

    return (
        <Modal
            title={`Edit ${user.firstName} ${user.lastName}`}
            onClose={() => !saving && onClose()}
            footer={
                <>
                    <Button variant="outline" size="sm" disabled={saving} onClick={onClose}>Cancel</Button>
                    <Button size="sm" loading={saving} onClick={handleSave}>Save</Button>
                </>
            }
        >
            <div className="form-row">
                <div className="form-group">
                    <label htmlFor="firstName">First name</label>
                    <input id="firstName" name="firstName" value={form.firstName} onChange={handleChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="lastName">Last name</label>
                    <input id="lastName" name="lastName" value={form.lastName} onChange={handleChange} />
                </div>
            </div>
            <div className="form-group">
                <label htmlFor="email">Email</label>
                <input id="email" name="email" type="email" value={form.email} onChange={handleChange} />
            </div>
            <div className="form-group">
                <label htmlFor="phone">Phone</label>
                <input id="phone" name="phone" value={form.phone} onChange={handleChange} />
            </div>
            <div className="form-row">
                <div className="form-group">
                    <label htmlFor="role">Role</label>
                    <select id="role" name="role" value={form.role} onChange={handleChange} disabled={isMe || isInstructor}>
                        {isInstructor && <option value="INSTRUCTOR">Instructor</option>}
                        {ROLES.map((r) => <option key={r} value={r}>{r.charAt(0) + r.slice(1).toLowerCase()}</option>)}
                    </select>
                </div>
                <div className="form-group">
                    <label htmlFor="userStatus">Status</label>
                    <select id="userStatus" name="userStatus" value={form.userStatus} onChange={handleChange} disabled={isMe}>
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                    </select>
                </div>
            </div>
            {isInstructor && <p className="muted">Instructors are managed on the Instructors page.</p>}
        </Modal>
    );
}